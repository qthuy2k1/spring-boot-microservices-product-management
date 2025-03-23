package com.qthuy2k1.userservice.unit.service;

import com.qthuy2k1.userservice.dto.request.RoleRequest;
import com.qthuy2k1.userservice.dto.response.PermissionResponse;
import com.qthuy2k1.userservice.dto.response.RoleResponse;
import com.qthuy2k1.userservice.enums.ErrorCode;
import com.qthuy2k1.userservice.mapper.RoleMapper;
import com.qthuy2k1.userservice.model.Permission;
import com.qthuy2k1.userservice.model.Role;
import com.qthuy2k1.userservice.repository.PermissionRepository;
import com.qthuy2k1.userservice.repository.RoleRepository;
import com.qthuy2k1.userservice.repository.UserRepository;
import com.qthuy2k1.userservice.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
public class RoleServiceTest extends AbstractIntegrationTest {
    private final RoleMapper roleMapper = Mappers.getMapper(RoleMapper.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private RoleService roleService;
    private Role roleSaved;
    private Permission permissionSaved1;
    private Permission permissionSaved2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        permissionSaved1 = permissionRepository.save(Permission.builder()
                .name("READ_DATA")
                .description("read data description")
                .build());
        permissionSaved2 = permissionRepository.save(Permission.builder()
                .name("WRITE_DATA")
                .description("write data description")
                .build());
        roleSaved = roleRepository.save(Role.builder()
                .name("ADMIN")
                .description("admin role")
                .permissions(Set.of(permissionSaved1, permissionSaved2))
                .build());
    }

    @Test
    void create_And_GetAll_Roles() {
        RoleRequest roleRequest = RoleRequest.builder()
                .name("USER")
                .description("user role")
                .permissions(Set.of(permissionSaved1.getName(), permissionSaved2.getName()))
                .build();

        RoleResponse roleCreate = roleService.create(roleRequest);

        List<RoleResponse> roles = roleService.getAll();
        assertThat(roles.size()).isEqualTo(2);

        // Get the newly inserted role which is at index 1
        RoleResponse roleResp = roles.get(1);
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
    void create_Role_NotFound_Permission() {
        RoleRequest roleRequest = RoleRequest.builder()
                .name("USER")
                .description("user role")
                .permissions(Set.of("MANAGER")) // permission doesn't exist in db
                .build();

        assertThatThrownBy(() ->
                roleService.create(roleRequest))
                .hasMessageContaining(ErrorCode.PERMISSION_NOT_FOUND.getMessage());
    }

    @Test
    void create_Role_Existed() {
        RoleRequest roleRequest = RoleRequest.builder()
                .name("ADMIN")
                .description("admin role")
                .permissions(Set.of(permissionSaved1.getName(), permissionSaved2.getName()))
                .build();

        assertThatThrownBy(() ->
                roleService.create(roleRequest))
                .hasMessageContaining(ErrorCode.ROLE_EXISTED.getMessage());
    }

    @Test
    void delete_Role() {
        roleService.delete(roleSaved.getName());
        List<RoleResponse> roles = roleService.getAll();
        assertThat(roles.size()).isEqualTo(0);
    }
}
