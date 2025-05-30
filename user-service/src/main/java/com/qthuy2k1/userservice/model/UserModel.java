package com.qthuy2k1.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users_tbl")
@FieldDefaults(level = AccessLevel.PRIVATE)
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
    Integer id;
    @NotEmpty(message = "USERNAME_NULL")
    String name;
    @Email(message = "EMAIL_INVALID")
    String email;
    @Size(min = 6, message = "PASSWORD_MIN")
    @NotEmpty(message = "PASSWORD_NULL")
    String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_tbl_roles",
            joinColumns = @JoinColumn(name = "users_tbl_id"),
            inverseJoinColumns = @JoinColumn(name = "roles_tbl_name")
    )
    @EqualsAndHashCode.Exclude
    Set<Role> roles = new HashSet<>();
}
