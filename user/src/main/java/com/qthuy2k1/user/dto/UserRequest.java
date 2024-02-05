package com.qthuy2k1.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "your name shouldn't be null")
    private String name;
    @NotBlank(message = "email address shouldn't be blank")
    @Email(message = "invalid email address")
    private String email;
    @Size(min = 6, message = "your password must have at least 6 characters")
    private String password;
}
