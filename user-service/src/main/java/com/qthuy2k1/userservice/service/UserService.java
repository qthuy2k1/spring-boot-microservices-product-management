package com.qthuy2k1.userservice.service;

import com.qthuy2k1.userservice.dto.request.UserRequest;
import com.qthuy2k1.userservice.dto.request.UserUpdateRequest;
import com.qthuy2k1.userservice.dto.response.UserResponse;
import com.qthuy2k1.userservice.enums.ErrorCode;
import com.qthuy2k1.userservice.enums.RoleEnum;
import com.qthuy2k1.userservice.event.UserCreated;
import com.qthuy2k1.userservice.exception.AppException;
import com.qthuy2k1.userservice.mapper.UserMapper;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    KafkaTemplate<String, UserCreated> kafkaTemplate;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    RoleRepository roleRepository;


    public UserResponse createUser(UserRequest userRequest) {
        // Check if user email already exists
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        UserModel user = userMapper.toUser(userRequest);

        // Encode the password using BCrypt
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        var role = roleRepository.findById(RoleEnum.USER.name())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND_SERVER));
        user.setRoles(new HashSet<>(List.of(role)));

        user = userRepository.save(user);

        // Produce the message to kafka
        kafkaTemplate.send("create-user", new UserCreated(user.getEmail()));

        return userMapper.toUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        List<UserModel> users = userRepository.findAll();

        return users.stream().map(userMapper::toUserResponse).toList();
    }

    @CacheEvict(cacheNames = "users", key = "#id")
    public void deleteUserById(Integer id) {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userRepository.delete(user);
    }

    @CachePut(cacheNames = "users", key = "#p0", condition = "#p0!=null")
    public UserResponse updateUserById(Integer id, UserUpdateRequest userRequest) {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));


        userMapper.updateUser(user, userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));


        var roles = roleRepository.findAllById(userRequest.getRoles());
        user.setRoles(new HashSet<>(roles));

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
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
    }

    @Cacheable(cacheNames = "users", key = "#p0", condition = "#p0!=null")
    public Boolean existsById(Integer id) {
        log.info("fetching from db");
        return userRepository.existsById(id);
    }

    @Cacheable(cacheNames = "users", key = "#p0", condition = "#p0!=null")
    public UserResponse getUserByEmail(String email) {
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        return userMapper.toUserResponse(user);
    }
}
