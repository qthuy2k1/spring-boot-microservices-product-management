package com.qthuy2k1.userservice.service;

import com.qthuy2k1.userservice.dto.request.RoleRequest;
import com.qthuy2k1.userservice.dto.response.RoleResponse;
import com.qthuy2k1.userservice.mapper.RoleMapper;
import com.qthuy2k1.userservice.model.Permission;
import com.qthuy2k1.userservice.model.Role;
import com.qthuy2k1.userservice.repository.PermissionRepository;
import com.qthuy2k1.userservice.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {
    private final RoleMapper roleMapper = Mappers.getMapper(RoleMapper.class);
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @InjectMocks
    private RoleService underTest;

    @BeforeEach
    void setUp() {
        underTest = new RoleService(roleRepository, roleMapper, permissionRepository);
    }

    @Test
    void create() {
        Permission permission = Permission.builder()
                .name("READ_DATA")
                .description("update data description")
                .build();
        RoleRequest roleRequest = RoleRequest.builder()
                .name("USER")
                .description("user role")
                .permissions(Set.of("READ_DATA"))
                .build();
        Role role = Role.builder()
                .name("USER")
                .description("user role")
                .permissions(Set.of(permission))
                .build();
        RoleResponse roleModelResp = roleMapper.toRoleResponse(role);

        given(permissionRepository.findAllById(roleRequest.getPermissions())).willReturn(List.of(permission));

        when(roleRepository.save(any())).thenReturn(role);
        RoleResponse roleResp = underTest.create(roleRequest);

        then(permissionRepository).should().findAllById(any());
        then(roleRepository).should().save(role);

        assertThat(roleResp.getName()).isEqualTo(roleModelResp.getName());
        assertThat(roleResp.getDescription()).isEqualTo(roleModelResp.getDescription());
        assertThat(roleResp.getPermissions()).isEqualTo(roleModelResp.getPermissions());
    }

    @Test
    void getAll() {
        underTest.getAll();
        then(roleRepository).should().findAll();
    }

    @Test
    void delete() {
        String role = "USER";
        underTest.delete(role);
        then(roleRepository).should().deleteById(role);
    }
}
