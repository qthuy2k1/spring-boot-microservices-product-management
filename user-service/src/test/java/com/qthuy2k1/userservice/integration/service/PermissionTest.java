package com.qthuy2k1.userservice.service;

import com.qthuy2k1.userservice.dto.request.PermissionRequest;
import com.qthuy2k1.userservice.dto.response.PermissionResponse;
import com.qthuy2k1.userservice.enums.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(properties = "spring.profiles.active=test")
@DirtiesContext
public class PermissionTest extends BaseServiceTest {
    @Autowired
    private IPermissionService permissionService;

    @Test
    void create_And_GetAll_Permission() {
        PermissionRequest permissionRequest = PermissionRequest.builder()
                .name("NEW_PERMISSION")
                .description("new permission description")
                .build();
        PermissionResponse permissionCreate = permissionService.create(permissionRequest);
        List<PermissionResponse> permissions = permissionService.getAll();
        assertThat(permissions.size()).isEqualTo(3);
        // Get the newly inserted permission which is at index 2
        PermissionResponse permissionResponse = permissions.get(2);

        assertThat(permissionCreate.getName()).isEqualTo(permissionResponse.getName());
        assertThat(permissionCreate.getDescription()).isEqualTo(permissionResponse.getDescription());
    }

    @Test
    void create_Permission_Existed() {
        PermissionRequest permissionRequest = PermissionRequest.builder()
                .name(permissionReadSaved.getName())
                .description("read data description")
                .build();
        assertThatThrownBy(() ->
                permissionService.create(permissionRequest))
                .hasMessageContaining(ErrorCode.PERMISSION_EXISTED.getMessage());
    }

    @Test
    void deletePermission() {
        // Remove the relationship between the role and the permission.
        // Delete adminRoleSaved which is only assigned to permissionWriteSaved
        roleRepository.findById(adminRoleSaved.getName()).ifPresent(role -> roleRepository.delete(role));

        permissionService.delete(permissionWriteSaved.getName());
        List<PermissionResponse> permissions = permissionService.getAll();
        assertThat(permissions.size()).isEqualTo(1);

        PermissionResponse permission = permissions.getFirst();
        assertThat(permission.getName()).isEqualTo(permissionReadSaved.getName());
        assertThat(permission.getDescription()).isEqualTo(permissionReadSaved.getDescription());
    }
}
