package com.qthuy2k1.userservice.integration.service;

import com.qthuy2k1.userservice.dto.request.RoleRequest;
import com.qthuy2k1.userservice.dto.response.PermissionResponse;
import com.qthuy2k1.userservice.dto.response.RoleResponse;
import com.qthuy2k1.userservice.enums.ErrorCode;
import com.qthuy2k1.userservice.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(properties = "spring.profiles.active=test")
@DirtiesContext
public class RoleServiceTest extends BaseServiceTest {
    @Autowired
    private RoleService roleService;

    @Test
    void create_And_GetAll_Roles() {
        RoleRequest roleRequest = RoleRequest.builder()
                .name("NEW_ROLE")
                .description("new role description")
                .permissions(Set.of(permissionReadSaved.getName(), permissionWriteSaved.getName()))
                .build();

        RoleResponse roleCreate = roleService.create(roleRequest);

        List<RoleResponse> roles = roleService.getAll();
        assertThat(roles.size()).isEqualTo(3);

        // Get the newly inserted role which is at index 2
        RoleResponse roleResp = roles.get(2);
        assertThat(roleCreate.getName()).isEqualTo(roleResp.getName());
        assertThat(roleCreate.getDescription()).isEqualTo(roleResp.getDescription());

        // Extract permission names from roleCreate
        Set<String> permissionNamesCreate = roleCreate.getPermissions().stream()
                .map(PermissionResponse::getName)
                .collect(Collectors.toSet());
        // Extract permission names from roles
        Set<String> permissionNamesResp = roleResp.getPermissions().stream()
                .map(PermissionResponse::getName)
                .collect(Collectors.toSet());

        // Compare the sets of permission names
        assertThat(permissionNamesCreate.equals(permissionNamesResp)).isTrue();
    }

    @Test
    void createRole_Existed() {
        RoleRequest roleRequest = RoleRequest.builder()
                .name(userRoleSaved.getName())
                .description("new role description")
                .permissions(Set.of(permissionReadSaved.getName(), permissionWriteSaved.getName()))
                .build();

        assertThatThrownBy(() ->
                roleService.create(roleRequest))
                .hasMessageContaining(ErrorCode.ROLE_EXISTED.getMessage());
    }

    @Test
    void deleteRole() {
        // Remove the relationship between the user and the role.
        userRepository.findById(userSaved.getId()).ifPresent(user -> userRepository.delete(user));

        roleService.delete(userRoleSaved.getName());
        List<RoleResponse> roles = roleService.getAll();
        assertThat(roles.size()).isEqualTo(1);

        RoleResponse role = roles.getFirst();
        assertThat(role.getName()).isEqualTo(adminRoleSaved.getName());
        assertThat(role.getDescription()).isEqualTo(adminRoleSaved.getDescription());

        Set<PermissionResponse> adminPermissions = adminRoleSaved.getPermissions()
                .stream()
                .map(permissionMapper::toPermissionResponse)
                .collect(Collectors.toSet());
        assertThat(role.getPermissions()).isEqualTo(adminPermissions);
    }
}
