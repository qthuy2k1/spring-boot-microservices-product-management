package com.qthuy2k1.userservice.dto;

import com.qthuy2k1.userservice.model.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse implements Serializable {
    @Min(value = 1, message = "ID must be greater than or equal to 1")
    private Integer id;
    @NotBlank(message = "your name shouldn't be null")
    private String name;
    @NotBlank(message = "email address shouldn't be blank")
    @Email(message = "invalid email address")
    private String email;
    @Enumerated(EnumType.STRING)
    private Role role;
}
