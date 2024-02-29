package com.qthuy2k1.userservice.service;

import com.qthuy2k1.userservice.dto.UserRequest;
import com.qthuy2k1.userservice.dto.UserResponse;
import com.qthuy2k1.userservice.event.UserCreated;
import com.qthuy2k1.userservice.exception.UserAlreadyExistsException;
import com.qthuy2k1.userservice.exception.UserNotFoundException;
import com.qthuy2k1.userservice.model.Role;
import com.qthuy2k1.userservice.model.UserModel;
import com.qthuy2k1.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final KafkaTemplate<String, UserCreated> kafkaTemplate;

    public void createUser(UserRequest userRequest) throws UserAlreadyExistsException {
        // Check if user email already exists
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserAlreadyExistsException("user already exists with email: " + userRequest.getEmail());
        }

        UserModel user = convertUserRequestToModel(userRequest);

        // Hash the password using BCrypt
        String pw_hash = BCrypt.hashpw(userRequest.getPassword(), BCrypt.gensalt(10));
        user.setPassword(pw_hash);

        userRepository.save(user);

        // Produce the message to kafka
        kafkaTemplate.send("create-user", new UserCreated(user.getEmail()));
    }

    public List<UserResponse> getAllUsers() {
        List<UserModel> users = userRepository.findAll();
        return users.stream().map(this::convertUserModelToResponse).toList();
    }

    public void deleteUserById(Integer id) throws UserNotFoundException {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("user not found with ID: " + id));

        userRepository.delete(user);
    }

    public void updateUserById(Integer id, UserRequest userRequest)
            throws UserNotFoundException, UserAlreadyExistsException {
        // Check if user email already exists
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserAlreadyExistsException("user already exists with email: " + userRequest.getEmail());
        }

        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("user not found with ID: " + id));

        // Update the user information based on the UserRequest
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());

        // Hash the password using BCrypt
        String pw_hash = BCrypt.hashpw(userRequest.getPassword(), BCrypt.gensalt(10));
        user.setPassword(pw_hash);

        userRepository.save(user);
    }

    public UserResponse getUserById(Integer id) throws UserNotFoundException {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("user not found with ID: " + id));

        return convertUserModelToResponse(user);
    }

    public Boolean existsById(Integer id) {
        return userRepository.existsById(id);
    }

    private UserModel convertUserRequestToModel(UserRequest userRequest) {
        return UserModel.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .role(Role.USER)
                .build();
    }

    private UserResponse convertUserModelToResponse(UserModel user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
