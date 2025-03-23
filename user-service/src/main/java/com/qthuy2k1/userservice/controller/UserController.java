package com.qthuy2k1.userservice.controller;

import com.qthuy2k1.userservice.dto.request.UserRequest;
import com.qthuy2k1.userservice.dto.request.UserUpdateRequest;
import com.qthuy2k1.userservice.dto.response.ApiResponse;
import com.qthuy2k1.userservice.dto.response.MessageResponse;
import com.qthuy2k1.userservice.dto.response.UserResponse;
import com.qthuy2k1.userservice.service.IUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final IUserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@RequestBody @Valid UserRequest userRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<UserResponse>builder()
                        .result(userService.createUser(userRequest))
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok().body(
                ApiResponse.<List<UserResponse>>builder()
                        .result(userService.getAllUsers())
                        .build()
        );
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable("id") int id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok().body(
                ApiResponse.<String>builder()
                        .result(MessageResponse.SUCCESS)
                        .build()
        );
    }

    @PutMapping("{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable("id") int id,
            @RequestBody @Valid UserUpdateRequest userRequest) {
        return ResponseEntity.ok().body(
                ApiResponse.<UserResponse>builder()
                        .result(userService.updateUserById(id, userRequest))
                        .build()
        );
    }

    @GetMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @PathVariable("id") int id) {
        return ResponseEntity.ok().body(
                ApiResponse.<UserResponse>builder()
                        .result(userService.getUserById(id))
                        .build()
        );
    }

    @GetMapping("/my-info")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo() {
        return ResponseEntity.ok().body(
                ApiResponse.<UserResponse>builder()
                        .result(userService.getMyInfo())
                        .build()
        );
    }

    @GetMapping("{id}/is-exists")
    public Boolean existsById(@PathVariable("id") int id) {
        return userService.existsById(id);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(
            @PathVariable("email") @Email(message = "EMAIL_INVALID") String email) {
        return ResponseEntity.ok().body(
                ApiResponse.<UserResponse>builder()
                        .result(userService.getUserByEmail(email))
                        .build()
        );
    }
}
