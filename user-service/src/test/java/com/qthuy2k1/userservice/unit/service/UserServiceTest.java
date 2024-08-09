package com.qthuy2k1.userservice.unit.service;

import com.qthuy2k1.userservice.dto.request.UserRequest;
import com.qthuy2k1.userservice.dto.request.UserUpdateRequest;
import com.qthuy2k1.userservice.dto.response.UserResponse;
import com.qthuy2k1.userservice.enums.ErrorCode;
import com.qthuy2k1.userservice.event.UserCreated;
import com.qthuy2k1.userservice.mapper.UserMapper;
import com.qthuy2k1.userservice.model.Permission;
import com.qthuy2k1.userservice.model.Role;
import com.qthuy2k1.userservice.model.UserModel;
import com.qthuy2k1.userservice.repository.RoleRepository;
import com.qthuy2k1.userservice.repository.UserRepository;
import com.qthuy2k1.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    @Mock
    private UserRepository userRepository;
    @Mock
    private KafkaTemplate<String, UserCreated> kafkaTemplate;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService underTest;
    @Mock
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        this.passwordEncoder = new BCryptPasswordEncoder();
        underTest = new UserService(
                userRepository, kafkaTemplate, passwordEncoder, userMapper, roleRepository);
    }

    @Test
    void createUser() {
        // given
        Permission permission = Permission.builder()
                .name("READ_DATA")
                .description("update data description")
                .build();
        Role role = Role.builder()
                .name("USER")
                .description("user role")
                .permissions(Set.of(permission))
                .build();
        UserRequest userRequest = UserRequest.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .build();
        UserModel userModel = userMapper.toUser(userRequest);
        userModel.setRoles(Set.of(role));

        given(roleRepository.findById(role.getName())).willReturn(Optional.of(role));

        // when
        when(userRepository.save(any())).thenReturn(userModel);
        underTest.createUser(userRequest);

        // then
        ArgumentCaptor<UserModel> userArgumentCaptor = ArgumentCaptor.forClass(UserModel.class);
        then(userRepository).should().save(userArgumentCaptor.capture());

        UserModel capturedUser = userArgumentCaptor.getValue();

        then(userRepository).should().existsByEmail(userRequest.getEmail());

        assertThat(capturedUser.getName()).isEqualTo(userRequest.getName());
        assertThat(capturedUser.getEmail()).isEqualTo(userRequest.getEmail());
        assertThat(capturedUser.getRoles()).isEqualTo(Set.of(role));
        assertThat(passwordEncoder.matches(
                userRequest.getPassword(), capturedUser.getPassword())).isTrue();
    }

    @Test()
    void createUser_WhenEmailIsTaken_ExceptionThrown() {
        // given
        UserRequest userRequest = UserRequest.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .build();
        given(userRepository.existsByEmail(userRequest.getEmail())).willReturn(true);

        // then
        then(userRepository).should(never()).save(any());
        assertThatThrownBy(() ->
                underTest.createUser(userRequest)).
                hasMessageContaining(ErrorCode.USER_EXISTED.getMessage());
    }

    @Test
    void getAllUsers() {
        // when
        underTest.getAllUsers();

        // then
        then(userRepository).should().findAll();
    }

    @Test
    void deleteUser() {
        // given
        Permission permission = Permission.builder()
                .name("READ_DATA")
                .description("update data description")
                .build();
        Role role = Role.builder()
                .name("USER")
                .description("user role")
                .permissions(Set.of(permission))
                .build();
        UserModel user = UserModel.builder()
                .id(1)
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .roles(Set.of(role))
                .build();

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        // when
        underTest.deleteUserById(user.getId());

        // then
        then(userRepository).should().delete(user);
    }

    @Test
    void deleteUserById_ExceptionThrown_UserNotFound() {
        // given
        int id = 1;
        given(userRepository.findById(id)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() ->
                underTest.deleteUserById(id)).
                hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());

        then(userRepository).should(never()).delete(any());
    }

    @Test
    void updateUserById() {
        // given
        int id = 1;
        Permission permission = Permission.builder()
                .name("READ_DATA")
                .description("update data description")
                .build();
        Set<Role> roles = Set.of(
                Role.builder()
                        .name("USER")
                        .description("user role")
                        .permissions(Set.of(permission))
                        .build()
        );
        UserUpdateRequest userRequest = UserUpdateRequest.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .roles(List.of("ADMIN"))
                .build();
        UserModel userModel = UserModel.builder()
                .id(1)
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .roles(roles)
                .build();

        UserModel userUpdatedModel = new UserModel();
        userMapper.updateUser(userUpdatedModel, userRequest);
        userUpdatedModel.setRoles(roles);

        given(userRepository.findById(id)).willReturn(Optional.of(userModel));
        given(roleRepository.findAllById(userRequest.getRoles())).willReturn(roles.stream().toList());

        // when
        when(userRepository.save(any())).thenReturn(userUpdatedModel);
        underTest.updateUserById(id, userRequest);

        // then
        ArgumentCaptor<UserModel> userArgumentCaptor = ArgumentCaptor.forClass(UserModel.class);
        then(userRepository).should().save(userArgumentCaptor.capture());

        UserModel capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser.getName()).isEqualTo(userRequest.getName());
        assertThat(capturedUser.getEmail()).isEqualTo(userRequest.getEmail());
        assertThat(capturedUser.getRoles()).isEqualTo(roles);
        assertThat(passwordEncoder.matches(
                userRequest.getPassword(), capturedUser.getPassword())).isTrue();
    }

    @Test
    void updateUserById_ExceptionThrown_UserNotFound() {
        // given
        int id = 1;
        UserUpdateRequest userRequest = UserUpdateRequest.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .roles(List.of("ADMIN"))
                .build();

        given(userRepository.findById(id)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() ->
                underTest.updateUserById(id, userRequest)).
                hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
        then(userRepository).should(never()).save(any());
    }

    @Test
    void updateUserById_ExceptionThrown_EmailAlreadyIsTaken() {
        // given
        int id = 1;
        UserUpdateRequest userRequest = UserUpdateRequest.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .roles(List.of("ADMIN"))
                .build();
        UserModel userModel = UserModel.builder()
                .id(1)
                .name("John Doe")
                .email("doee@gmail.com")
                .password("123123")
                .roles(Set.of())
                .build();

        given(userRepository.findById(id)).willReturn(Optional.of(userModel));
        given(userRepository.existsByEmail(userRequest.getEmail())).willReturn(true);

        // then
        assertThatThrownBy(() ->
                underTest.updateUserById(id, userRequest)).
                hasMessageContaining(ErrorCode.USER_EXISTED.getMessage());
        then(userRepository).should(never()).save(any());
    }


    @Test
    void getUserById() {
        // given
        int id = 1;
        Permission permission = Permission.builder()
                .name("READ_DATA")
                .description("update data description")
                .build();
        Role role = Role.builder()
                .name("USER")
                .description("user role")
                .permissions(Set.of(permission))
                .build();
        UserModel userModel = UserModel.builder()
                .id(1)
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .roles(Set.of(role))
                .build();
        UserResponse userModelResp = userMapper.toUserResponse(userModel);

        given(userRepository.findById(id)).willReturn(Optional.of(userModel));

        // when
        UserResponse userResp = underTest.getUserById(id);

        // then
        assertThat(userResp.getId()).isEqualTo(userModelResp.getId());
        assertThat(userResp.getName()).isEqualTo(userModelResp.getName());
        assertThat(userResp.getEmail()).isEqualTo(userModelResp.getEmail());
        assertThat(userResp.getRoles()).isEqualTo(userModelResp.getRoles());

        then(userRepository).should().findById(id);
    }


    @Test
    void getUserById_ExceptionThrown_UserNotFound() {
        // given
        int id = 1;
        given(userRepository.findById(id)).willReturn(Optional.empty());

        // when
        assertThatThrownBy(() ->
                underTest.getUserById(id))
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());

        // then
        then(userRepository).should().findById(id);
    }

    @Test
    void getMyInfo() {
        // given
        String email = "user@gmail.com";
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Permission permission = Permission.builder()
                .name("READ_DATA")
                .description("update data description")
                .build();
        Role role = Role.builder()
                .name("USER")
                .description("user role")
                .permissions(Set.of(permission))
                .build();
        UserModel userModel = UserModel.builder()
                .id(1)
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .roles(Set.of(role))
                .build();
        UserResponse userModelResp = userMapper.toUserResponse(userModel);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(userModel));

        // when
        UserResponse user = underTest.getMyInfo();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(userModelResp.getId());
        assertThat(user.getName()).isEqualTo(userModelResp.getName());
        assertThat(user.getRoles()).isEqualTo(userModelResp.getRoles());
    }

    @Test
    void existsById() {
        // given
        int id = 1;
        given(userRepository.existsById(id)).willReturn(true);

        // when
        boolean isUserExists = underTest.existsById(id);

        // then
        assertThat(isUserExists).isTrue();
        then(userRepository).should().existsById(id);
    }

    @Test
    void existsById_NotExists() {
        // given
        int id = 1;
        given(userRepository.existsById(id)).willReturn(false);

        // when
        boolean isUserExists = underTest.existsById(id);

        // then
        assertThat(isUserExists).isFalse();
        then(userRepository).should().existsById(id);
    }

    @Test
    void getUserByEmail() {
        // given
        String email = "user@gmail.com";
        Permission permission = Permission.builder()
                .name("READ_DATA")
                .description("update data description")
                .build();
        Role role = Role.builder()
                .name("USER")
                .description("user role")
                .permissions(Set.of(permission))
                .build();
        UserModel userModel = UserModel.builder()
                .id(1)
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .roles(Set.of(role))
                .build();
        UserResponse userModelResp = userMapper.toUserResponse(userModel);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(userModel));

        // when
        UserResponse userResp = underTest.getUserByEmail(email);

        // then
        assertThat(userResp.getId()).isEqualTo(userModelResp.getId());
        assertThat(userResp.getName()).isEqualTo(userModelResp.getName());
        assertThat(userResp.getEmail()).isEqualTo(userModelResp.getEmail());
        assertThat(userResp.getRoles()).isEqualTo(userModelResp.getRoles());

        then(userRepository).should().findByEmail(email);
    }

    @Test
    void getUserByEmail_UserNotFound() {
        // given
        String email = "user@gmail.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() ->
                underTest.getUserByEmail(email))
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
        then(userRepository).should().findByEmail(email);
    }
}