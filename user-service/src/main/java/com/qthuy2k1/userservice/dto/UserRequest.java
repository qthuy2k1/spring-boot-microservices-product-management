package com.qthuy2k1.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {
    @NotNull(message = "your name shouldn't be null")
    @NotBlank(message = "your name shouldn't be blank")
    private String name;
    @NotNull(message = "your email address shouldn't be null")
    @NotBlank(message = "your email address shouldn't be blank")
    @Email(message = "invalid email address")
    private String email;
    @Size(min = 6, message = "your password must have at least 6 characters")
    @NotNull(message = "your email address shouldn't be null")
    private String password;
}
