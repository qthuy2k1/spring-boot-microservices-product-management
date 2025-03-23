package com.qthuy2k1.userservice.unit.service;

import com.qthuy2k1.userservice.dto.request.UserRequest;
import com.qthuy2k1.userservice.dto.request.UserUpdateRequest;
import com.qthuy2k1.userservice.dto.response.RoleResponse;
import com.qthuy2k1.userservice.dto.response.UserResponse;
import com.qthuy2k1.userservice.enums.ErrorCode;
import com.qthuy2k1.userservice.event.UserCreated;
import com.qthuy2k1.userservice.mapper.RoleMapper;
import com.qthuy2k1.userservice.mapper.UserMapper;
import com.qthuy2k1.userservice.model.Permission;
import com.qthuy2k1.userservice.model.Role;
import com.qthuy2k1.userservice.model.UserModel;
import com.qthuy2k1.userservice.repository.PermissionRepository;
import com.qthuy2k1.userservice.repository.RoleRepository;
import com.qthuy2k1.userservice.repository.UserRepository;
import com.qthuy2k1.userservice.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
class UserServiceTest extends AbstractIntegrationTest {
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    private final RoleMapper roleMapper = Mappers.getMapper(RoleMapper.class);
    @Autowired
    private UserRepository userRepository;
    @MockBean
    private KafkaTemplate<String, UserCreated> kafkaTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private IUserService userService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    private UserModel userSaved;
    private Role roleSaved;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        Permission permissionSaved = permissionRepository.save(Permission.builder()
                .name("READ_DATA")
                .description("update data description")
                .build());
        roleSaved = roleRepository.save(Role.builder()
                .name("USER")
                .description("user role")
                .permissions(Set.of(permissionSaved))
                .build());
        userSaved = userRepository.save(UserModel.builder()
                .name("user 9999")
                .email("user9999@gmail.com")
                .password("123123")
                .roles(Set.of(roleSaved))
                .build());
    }

    @Test
    public void testConnection() {
        assertThat(postgres.isRunning()).isTrue();
        assertThat(REDIS_CONTAINER.isRunning()).isTrue();
    }

    @Test
    void create_And_GetAll_Users() {
        // given
        UserRequest userRequest = UserRequest.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .build();

        UserResponse userCreate = userService.createUser(userRequest);

        List<UserResponse> users = userService.getAllUsers();
        // Get the newly inserted user which is at index 1
        UserResponse userResp = users.get(1);

        assertThat(users.size()).isEqualTo(2);
        assertThat(userResp.getName()).isEqualTo(userCreate.getName());
        assertThat(userResp.getEmail()).isEqualTo(userCreate.getEmail());
        assertThat(userResp.getRoles()).isEqualTo(Set.of());
    }

    @Test()
    void createUser_WhenEmailIsTaken_ExceptionThrown() {
        // given
        UserRequest userRequest = UserRequest.builder()
                .name("John Doe")
                .email("user9999@gmail.com")
                .password("123123")
                .build();

        assertThatThrownBy(() ->
                userService.createUser(userRequest))
                .hasMessageContaining(ErrorCode.USER_EXISTED.getMessage());
    }

    @Test
    void deleteUser() {
        userService.deleteUserById(userSaved.getId());

        List<UserResponse> users = userService.getAllUsers();
        // The user list size should be 0
        assertThat(users.size()).isEqualTo(0);

        assertThatThrownBy(() ->
                userService.getUserById(userSaved.getId())).
                hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());

    }

    @Test
    void deleteUserById_ExceptionThrown_UserNotFound() {
        // given
        int id = userSaved.getId() + 1;
        assertThatThrownBy(() ->
                userService.deleteUserById(id)).
                hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void updateUserById() {
        UserUpdateRequest userRequest = UserUpdateRequest.builder()
                .name("user 9998")
                .email("user9998@gmail.com")
                .password("123123")
                .roles(List.of(roleSaved.getName()))
                .build();

        userService.updateUserById(userSaved.getId(), userRequest);

        UserResponse user = userService.getUserById(userSaved.getId());

        assertThat(user.getName()).isEqualTo(userRequest.getName());
        assertThat(user.getEmail()).isEqualTo(userRequest.getEmail());
        assertThat(user.getRoles()).isEqualTo(Set.of(roleMapper.toRoleResponse(roleSaved)));
    }

    @Test
    void updateUserById_ExceptionThrown_UserNotFound() {
        int id = userSaved.getId() + 1;
        UserUpdateRequest userRequest = UserUpdateRequest.builder()
                .name("user 9998")
                .email("user9998@gmail.com")
                .password("123123")
                .roles(List.of(roleSaved.getName()))
                .build();

        assertThatThrownBy(() ->
                userService.updateUserById(id, userRequest)).
                hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void updateUserById_ExceptionThrown_EmailAlreadyIsTaken() {
        // create another user
        userService.createUser(UserRequest.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .build());

        UserUpdateRequest userRequest = UserUpdateRequest.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .roles(List.of())
                .build();

        assertThatThrownBy(() ->
                userService.updateUserById(userSaved.getId(), userRequest)).
                hasMessageContaining(ErrorCode.USER_EXISTED.getMessage());
    }


    @Test
    void getUserById() {
        UserResponse userResp = userService.getUserById(userSaved.getId());

        List<RoleResponse> userSavedRoleResponse = userSaved.getRoles()
                .stream()
                .map(roleMapper::toRoleResponse)
                .toList();

        assertThat(userResp.getId()).isEqualTo(userSaved.getId());
        assertThat(userResp.getName()).isEqualTo(userSaved.getName());
        assertThat(userResp.getEmail()).isEqualTo(userSaved.getEmail());
        assertThat(userResp.getRoles().toString()).isEqualTo(userSavedRoleResponse.toString());
    }


    @Test
    void getUserById_ExceptionThrown_UserNotFound() {
        int id = userSaved.getId() + 1;
        assertThatThrownBy(() ->
                userService.getUserById(id))
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void getMyInfo() {
        // given
        String email = userSaved.getEmail();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserResponse user = userService.getMyInfo();

        List<RoleResponse> userSavedRoleResponse = userSaved.getRoles()
                .stream()
                .map(roleMapper::toRoleResponse)
                .toList();

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(userSaved.getId());
        assertThat(user.getName()).isEqualTo(userSaved.getName());
        assertThat(user.getEmail()).isEqualTo(userSaved.getEmail());
        assertThat(user.getRoles().toString()).isEqualTo(userSavedRoleResponse.toString());
    }

    @Test
    void existsById() {
        boolean isUserExists = userService.existsById(userSaved.getId());
        assertThat(isUserExists).isTrue();
    }

    @Test
    void existsById_NotExists() {
        int id = userSaved.getId() + 1;
        boolean isUserExists = userService.existsById(id);
        assertThat(isUserExists).isFalse();
    }

    @Test
    void getUserByEmail() {
        String email = userSaved.getEmail();
        UserResponse user = userService.getUserByEmail(email);
        List<RoleResponse> userSavedRoleResponse = userSaved.getRoles()
                .stream()
                .map(roleMapper::toRoleResponse)
                .toList();

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(userSaved.getId());
        assertThat(user.getName()).isEqualTo(userSaved.getName());
        assertThat(user.getEmail()).isEqualTo(userSaved.getEmail());
        assertThat(user.getRoles().toString()).isEqualTo(userSavedRoleResponse.toString());
    }

    @Test
    void getUserByEmail_UserNotFound() {
        String email = "notfound" + userSaved.getEmail();
        assertThatThrownBy(() ->
                userService.getUserByEmail(email))
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }
}