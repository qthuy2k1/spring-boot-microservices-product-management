package com.qthuy2k1.userservice.dto.request;

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
    @NotNull(message = "USERNAME_NULL")
    @NotBlank(message = "USERNAME_BLANK")
    private String name;
    @NotNull(message = "EMAIL_NULL")
    @NotBlank(message = "EMAIL_BLANK")
    @Email(message = "EMAIL_INVALID")
    private String email;
    @Size(min = 6, message = "PASSWORD_MIN")
    @NotNull(message = "PASSWORD_NULL")
    private String password;
}
