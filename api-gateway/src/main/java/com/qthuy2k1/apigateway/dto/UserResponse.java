package com.qthuy2k1.apigateway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    @Min(value = 1, message = "ID must be greater than or equal to 1")
    private Integer id;
    @NotBlank(message = "your name shouldn't be null")
    private String name;
    @NotBlank(message = "email address shouldn't be blank")
    @Email(message = "invalid email address")
    private String email;
    private String role;
}
