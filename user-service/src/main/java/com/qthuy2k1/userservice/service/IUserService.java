package com.qthuy2k1.userservice.service;

import com.qthuy2k1.userservice.dto.request.UserRequest;
import com.qthuy2k1.userservice.dto.request.UserUpdateRequest;
import com.qthuy2k1.userservice.dto.response.UserResponse;

import java.util.List;

public interface IUserService {
    UserResponse createUser(UserRequest userRequest);

    UserResponse handleNotificationFallback(UserRequest userRequest, Throwable t);

    List<UserResponse> getAllUsers();

    void deleteUserById(Integer id);

    UserResponse updateUserById(Integer id, UserUpdateRequest userRequest);

    UserResponse getUserById(Integer id);

    UserResponse getMyInfo();

    Boolean existsById(Integer id);

    UserResponse getUserByEmail(String email);
}
