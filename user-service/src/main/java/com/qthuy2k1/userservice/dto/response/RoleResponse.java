package com.qthuy2k1.userservice.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleResponse implements Serializable {
    String name;
    String description;
    Set<PermissionResponse> permissions;
}
