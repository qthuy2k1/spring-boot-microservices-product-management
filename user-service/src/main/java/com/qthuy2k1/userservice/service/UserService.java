package com.qthuy2k1.userservice.service;

import com.qthuy2k1.userservice.dto.request.RoleRequest;
import com.qthuy2k1.userservice.dto.request.UserRequest;
import com.qthuy2k1.userservice.dto.request.UserUpdateRequest;
import com.qthuy2k1.userservice.dto.response.UserResponse;
import com.qthuy2k1.userservice.enums.ErrorCode;
import com.qthuy2k1.userservice.enums.RoleEnum;
import com.qthuy2k1.userservice.event.UserCreated;
import com.qthuy2k1.userservice.exception.AppException;
import com.qthuy2k1.userservice.mapper.RoleMapper;
import com.qthuy2k1.userservice.mapper.UserMapper;
import com.qthuy2k1.userservice.model.Role;
import com.qthuy2k1.userservice.model.UserModel;
import com.qthuy2k1.userservice.repository.RoleRepository;
import com.qthuy2k1.userservice.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService implements IUserService {
    UserRepository userRepository;
    KafkaTemplate<String, UserCreated> kafkaTemplate;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    RoleRepository roleRepository;
    IRoleService roleService;
    RoleMapper roleMapper;


    public UserResponse createUser(UserRequest userRequest) {
        // Check if user email already existed
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        UserModel user = userMapper.toUser(userRequest);

        // Encode the password using BCrypt
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        // Check if user role already existed
        Optional<Role> roleOptional = roleRepository.findById(RoleEnum.USER.name());
        Role role = roleOptional.orElseGet(() -> roleMapper.toRole(roleService.create(
                RoleRequest.builder()
                        .name(RoleEnum.USER.name())
                        .description("user role description")
                        .permissions(Set.of())
                        .build()
        )));

        user.setRoles(Set.of(role));
        user = userRepository.save(user);

        // Produce the message to kafka
        kafkaTemplate.send("create-user", new UserCreated(user.getEmail()));

        return userMapper.toUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        List<UserModel> users = userRepository.findAll();
        return users.stream().map(userMapper::toUserResponse).toList();
    }

    @CacheEvict(cacheNames = "users", key = "#p0", condition = "#p0!=null")
    public void deleteUserById(Integer id) {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }

    @CachePut(cacheNames = "users", key = "#p0", condition = "#p0!=null")
    public UserResponse updateUserById(Integer id, UserUpdateRequest userRequest) {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!user.getEmail().equals(userRequest.getEmail())
                && userRepository.existsByEmail(userRequest.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        userMapper.updateUser(user, userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        if (!userRequest.getRoles().isEmpty()) {
            List<Role> roles = roleRepository.findAllById(userRequest.getRoles());
            if (!roles.isEmpty()) {
                user.setRoles(new HashSet<>(roles));
            }
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Cacheable(cacheNames = "users", key = "#p0", condition = "#p0!=null")
    public UserResponse getUserById(Integer id) {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    @Cacheable(cacheNames = "users", key = "#p0", condition = "#p0!=null")
    public UserResponse getMyInfo() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    @Cacheable(cacheNames = "users", key = "#p0", condition = "#p0!=null")
    public Boolean existsById(Integer id) {
        return userRepository.existsById(id);
    }

    @Cacheable(cacheNames = "users", key = "#p0", condition = "#p0!=null")
    public UserResponse getUserByEmail(String email) {
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }
}
