package com.qthuy2k1.userservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission {
    @Id
    @Column(nullable = false, unique = true, name = "name")
    String name;
    String description;

    @ManyToMany(mappedBy = "permissions")
    @EqualsAndHashCode.Exclude
    Set<Role> roles = new HashSet<>();
}
