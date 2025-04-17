package com.qthuy2k1.userservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.userservice.UserApplication;
import com.qthuy2k1.userservice.dto.request.RoleRequest;
import com.qthuy2k1.userservice.dto.response.ApiResponse;
import com.qthuy2k1.userservice.dto.response.MessageResponse;
import com.qthuy2k1.userservice.dto.response.RoleResponse;
import com.qthuy2k1.userservice.enums.ErrorCode;
import com.qthuy2k1.userservice.mapper.PermissionMapper;
import com.qthuy2k1.userservice.mapper.RoleMapper;
import com.qthuy2k1.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = UserApplication.class,
        properties = "spring.profiles.active=test"
)
@ExtendWith(SpringExtension.class)
@DirtiesContext
public class RoleControllerTest extends BaseControllerTest {
    private final RoleMapper roleMapper = Mappers.getMapper(RoleMapper.class);
    private final PermissionMapper permissionMapper = Mappers.getMapper(PermissionMapper.class);
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    @Test
    void create_And_GetAll_And_Delete_Role() throws Exception {
        String authToken = login();
        RoleRequest roleRequest = RoleRequest.builder()
                .name("NEW ROLE")
                .description("new role description")
                .permissions(Set.of(permissionSaved.getName()))
                .build();

        RoleResponse expectedRoleResponse = RoleResponse.builder()
                .name("NEW ROLE")
                .description("new role description")
                .permissions(Set.of(permissionMapper.toPermissionResponse(permissionSaved)))
                .build();

        MvcResult createRoleResult = mockMvc.perform(post("/roles")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn();

        // Deserialize the JSON response into ApiResponse<RoleResponse>
        ApiResponse<RoleResponse> createRoleApiResponse = objectMapper.readValue(
                createRoleResult.getResponse().getContentAsByteArray(),
                new TypeReference<ApiResponse<RoleResponse>>() {
                }
        );

        assertThat(createRoleApiResponse).isNotNull();
        assertThat(createRoleApiResponse.getCode()).isEqualTo(DEFAULT_CODE_RESPONSE);
        assertThat(createRoleApiResponse.getResult()).isNotNull();
        assertThat(createRoleApiResponse.getResult().getName()).isEqualTo(expectedRoleResponse.getName());
        assertThat(createRoleApiResponse.getResult().getDescription()).isEqualTo(expectedRoleResponse.getDescription());
        assertThat(createRoleApiResponse.getResult().getPermissions()).isEqualTo(expectedRoleResponse.getPermissions());

        // getAllRoles method
        MvcResult getAllRolesResult = mockMvc.perform(get("/roles")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        ApiResponse<List<RoleResponse>> getAllRolesApiResponse = objectMapper.readValue(
                getAllRolesResult.getResponse().getContentAsByteArray(),
                new TypeReference<ApiResponse<List<RoleResponse>>>() {
                }
        );

        assertThat(getAllRolesApiResponse).isNotNull();
        assertThat(getAllRolesApiResponse.getResult().size()).isEqualTo(3);

        RoleResponse roleCreated = getAllRolesApiResponse.getResult().getLast();
        assertThat(roleCreated.getName()).isEqualTo(createRoleApiResponse.getResult().getName());
        assertThat(roleCreated.getDescription()).isEqualTo(createRoleApiResponse.getResult().getDescription());
        assertThat(roleCreated.getPermissions()).isEqualTo(createRoleApiResponse.getResult().getPermissions());

        // for deleteRole method
        ApiResponse<String> deleteRoleApiResponse = ApiResponse.<String>builder()
                .result(MessageResponse.SUCCESS)
                .build();
        mockMvc.perform(MockMvcRequestBuilders.delete("/roles/" + roleCreated.getName())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(deleteRoleApiResponse)))
                .andDo(print());


        // for getAllRoles method
        MvcResult getAllRolesResult2 = mockMvc.perform(get("/roles")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        ApiResponse<List<RoleResponse>> getAllRolesApiResponse2 = objectMapper.readValue(
                getAllRolesResult2.getResponse().getContentAsByteArray(),
                new TypeReference<ApiResponse<List<RoleResponse>>>() {
                }
        );

        assertThat(getAllRolesApiResponse2).isNotNull();
        // assert that the created role is deleted
        assertThat(getAllRolesApiResponse2.getResult().size()).isEqualTo(2);
    }

    @Test
    void createRole_RoleExisted() throws Exception {
        String authToken = login();
        RoleRequest roleRequest = RoleRequest.builder()
                .name(userRoleSaved.getName())
                .description("new role description")
                .permissions(Set.of("READ_DATA"))
                .build();

        ApiResponse<String> createRoleApiResponse = ApiResponse.<String>builder()
                .code(ErrorCode.ROLE_EXISTED.getCode())
                .message(ErrorCode.ROLE_EXISTED.getMessage())
                .build();
        mockMvc.perform(post("/roles")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(content().string(objectMapper.writeValueAsString(createRoleApiResponse)));
    }

}
