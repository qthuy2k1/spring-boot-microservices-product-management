package com.qthuy2k1.user.service;

import com.qthuy2k1.user.dto.UserRequest;
import com.qthuy2k1.user.dto.UserResponse;
import com.qthuy2k1.user.exception.UserAlreadyExistsException;
import com.qthuy2k1.user.exception.UserNotFoundException;
import com.qthuy2k1.user.model.Role;
import com.qthuy2k1.user.model.UserModel;
import com.qthuy2k1.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

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
        kafkaTemplate.send("create-user", user.getEmail());
    }

    public List<UserResponse> getAllUsers() {
        List<UserModel> users = userRepository.findAll();
        return users.stream().map(this::convertUserModelToResponse).toList();
    }

    public void deleteUserById(String id) throws UserNotFoundException {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("user not found with ID: " + id));

        userRepository.delete(user);
    }

    public void updateUserById(String id, UserRequest userRequest)
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

    public UserResponse getUserById(String id) throws UserNotFoundException {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("user not found with ID: " + id));

        return convertUserModelToResponse(user);
    }

    public Boolean existsById(String id) {
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
