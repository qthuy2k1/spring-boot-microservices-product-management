package com.qthuy2k1.userservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Role {
    @Id
    @Column(nullable = false, unique = true)
    String name;
    String description;

    @ManyToMany(mappedBy = "roles")
    @EqualsAndHashCode.Exclude
    Set<UserModel> users = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "roles_tbl_permissions",
            joinColumns = @JoinColumn(name = "roles_tbl_name"),
            inverseJoinColumns = @JoinColumn(name = "permissions_tbl_name")
    )
    @EqualsAndHashCode.Exclude
    Set<Permission> permissions = new HashSet<>();
}
