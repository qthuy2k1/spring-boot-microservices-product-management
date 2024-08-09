package com.qthuy2k1.userservice.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.userservice.controller.RoleController;
import com.qthuy2k1.userservice.dto.request.RoleRequest;
import com.qthuy2k1.userservice.dto.response.ApiResponse;
import com.qthuy2k1.userservice.dto.response.MessageResponse;
import com.qthuy2k1.userservice.dto.response.PermissionResponse;
import com.qthuy2k1.userservice.dto.response.RoleResponse;
import com.qthuy2k1.userservice.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RoleController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class RoleControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private RoleService roleService;
    @InjectMocks
    private RoleController roleController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void create() throws Exception {
        // given
        RoleRequest roleRequest = RoleRequest.builder()
                .name("USER")
                .description("user description")
                .permissions(Set.of("READ_DATA"))
                .build();
        PermissionResponse permissionResponse = PermissionResponse.builder()
                .name("READ_DATA")
                .description("read data description")
                .build();

        RoleResponse roleResponse = RoleResponse.builder()
                .name("USER")
                .description("user description")
                .permissions(Set.of(permissionResponse))
                .build();

        given(roleService.create(any())).willReturn(roleResponse);

        // when
        ApiResponse<RoleResponse> apiResponse = ApiResponse.<RoleResponse>builder()
                .result(roleResponse)
                .build();

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(roleService).should().create(any());
    }

    @Test
    void getAll() throws Exception {
        // given
        List<RoleResponse> roles = new ArrayList<>();
        PermissionResponse permissionResponse = PermissionResponse.builder()
                .name("READ_DATA")
                .description("read data description")
                .build();

        roles.add(
                RoleResponse.builder()
                        .name("USER")
                        .description("user description")
                        .permissions(Set.of(permissionResponse))
                        .build()
        );
        roles.add(
                RoleResponse.builder()
                        .name("ADMIN")
                        .description("admin description")
                        .permissions(Set.of(permissionResponse))
                        .build()
        );
        given(roleService.getAll()).willReturn(roles);

        // when
        ApiResponse<List<RoleResponse>> apiResponse = ApiResponse.<List<RoleResponse>>builder()
                .result(roles)
                .build();

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)));

        // then
        then(roleService).should().getAll();
    }

    @Test
    void delete() throws Exception {
        // given
        String name = "USER";

        // when
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .result(MessageResponse.SUCCESS)
                .build();
        mockMvc.perform(MockMvcRequestBuilders.delete("/roles/" + name)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(roleService).should().delete(name);
    }
}
