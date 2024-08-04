package com.qthuy2k1.userservice.service;

import com.qthuy2k1.userservice.dto.request.PermissionRequest;
import com.qthuy2k1.userservice.dto.response.PermissionResponse;
import com.qthuy2k1.userservice.mapper.PermissionMapper;
import com.qthuy2k1.userservice.model.Permission;
import com.qthuy2k1.userservice.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class PermissionTest {
    private final PermissionMapper permissionMapper = Mappers.getMapper(PermissionMapper.class);
    @Mock
    private PermissionRepository permissionRepository;
    @InjectMocks
    private PermissionService underTest;

    @BeforeEach
    void setUp() {
        underTest = new PermissionService(permissionRepository, permissionMapper);
    }

    @Test
    void create() {
        PermissionRequest permissionRequest = PermissionRequest.builder()
                .name("READ_DATA")
                .description("update data description")
                .build();
        Permission permission = Permission.builder()
                .name("READ_DATA")
                .description("update data description")
                .build();
        PermissionResponse permissionModelResp = permissionMapper.toPermissionResponse(permission);

        when(permissionRepository.save(any())).thenReturn(permission);
        PermissionResponse permissionResp = underTest.create(permissionRequest);

        then(permissionRepository).should().save(permission);
        assertThat(permissionResp.getName()).isEqualTo(permissionModelResp.getName());
        assertThat(permissionResp.getDescription()).isEqualTo(permissionModelResp.getDescription());
    }

    @Test
    void getAll() {
        underTest.getAll();
        then(permissionRepository).should().findAll();
    }

    @Test
    void delete() {
        String permission = "READ_DATA";
        underTest.delete(permission);
        then(permissionRepository).should().deleteById(permission);
    }
}
