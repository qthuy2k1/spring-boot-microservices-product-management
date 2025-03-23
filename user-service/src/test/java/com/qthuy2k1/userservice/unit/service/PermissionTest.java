package com.qthuy2k1.userservice.unit.service;

import com.qthuy2k1.userservice.dto.request.PermissionRequest;
import com.qthuy2k1.userservice.dto.response.PermissionResponse;
import com.qthuy2k1.userservice.enums.ErrorCode;
import com.qthuy2k1.userservice.mapper.PermissionMapper;
import com.qthuy2k1.userservice.model.Permission;
import com.qthuy2k1.userservice.repository.PermissionRepository;
import com.qthuy2k1.userservice.repository.RoleRepository;
import com.qthuy2k1.userservice.repository.UserRepository;
import com.qthuy2k1.userservice.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
public class PermissionTest extends AbstractIntegrationTest {
    private final PermissionMapper permissionMapper = Mappers.getMapper(PermissionMapper.class);
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    private Permission permissionSaved;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionSaved = permissionRepository.save(Permission.builder()
                .name("READ_DATA")
                .description("read data description")
                .build());
    }

    @Test
    void create_And_GetAll_Permission() {
        PermissionRequest permissionRequest = PermissionRequest.builder()
                .name("WRITE_DATA")
                .description("write data description")
                .build();
        PermissionResponse permissionCreate = permissionService.create(permissionRequest);
        List<PermissionResponse> permissions = permissionService.getAll();
        // Get the newly inserted permission which is at index 1
        PermissionResponse permissionResponse = permissions.get(1);

        assertThat(permissionCreate.getName()).isEqualTo(permissionResponse.getName());
        assertThat(permissionCreate.getDescription()).isEqualTo(permissionResponse.getDescription());
    }

    @Test
    void create_Permission_Existed() {
        PermissionRequest permissionRequest = PermissionRequest.builder()
                .name("READ_DATA")
                .description("read data description")
                .build();
        assertThatThrownBy(() ->
                permissionService.create(permissionRequest))
                .hasMessageContaining(ErrorCode.PERMISSION_EXISTED.getMessage());
    }

    @Test
    void delete() {
        permissionService.delete(permissionSaved.getName());
        List<PermissionResponse> permissions = permissionService.getAll();
        assertThat(permissions.size()).isEqualTo(0);
    }
}
