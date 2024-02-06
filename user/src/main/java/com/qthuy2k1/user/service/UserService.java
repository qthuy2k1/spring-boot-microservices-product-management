package com.qthuy2k1.user.service;

import com.qthuy2k1.user.dto.UserRequest;
import com.qthuy2k1.user.dto.UserResponse;
import com.qthuy2k1.user.exception.UserAlreadyExistsException;
import com.qthuy2k1.user.exception.UserNotFoundException;
import com.qthuy2k1.user.model.Role;
import com.qthuy2k1.user.model.UserModel;
import com.qthuy2k1.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

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
    }

    public List<UserResponse> getAllUsers() {
        List<UserModel> users = userRepository.findAll();
        return users.stream().map(this::convertUserModelToResponse).toList();
    }

    public void deleteUserById(String id) throws UserNotFoundException {
        Optional<UserModel> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            UserModel user = userOptional.get();
            userRepository.delete(user);
        } else {
            throw new UserNotFoundException("user not found with ID: " + id);
        }
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
