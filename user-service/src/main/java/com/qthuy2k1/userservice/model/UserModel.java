package com.qthuy2k1.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users_tbl")
public class UserModel {
    @Id
    @SequenceGenerator(
            name = "user_id_sequence",
            sequenceName = "user_id_sequence"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_id_sequence"
    )
    private Integer id;
    @NotEmpty(message = "your name shouldn't be null")
    private String name;
    @Email(message = "invalid email address")
    private String email;
    @Size(min = 6, message = "{validation.name.size.too_short}")
    @NotEmpty(message = "your password shouldn't be null")
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

    public UserModel(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
