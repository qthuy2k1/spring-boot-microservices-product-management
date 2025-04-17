package com.qthuy2k1.userservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.userservice.UserApplication;
import com.qthuy2k1.userservice.dto.request.PermissionRequest;
import com.qthuy2k1.userservice.dto.response.ApiResponse;
import com.qthuy2k1.userservice.dto.response.MessageResponse;
import com.qthuy2k1.userservice.dto.response.PermissionResponse;
import com.qthuy2k1.userservice.enums.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

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
public class PermissionControllerTest extends BaseControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_And_GetAll_And_Delete_Permission() throws Exception {
        String authToken = login();
        PermissionRequest permissionRequest = PermissionRequest.builder()
                .name("NEW_PERMISSION")
                .description("new permission description")
                .build();
        PermissionResponse expectedPermissionResponse = PermissionResponse.builder()
                .name("NEW_PERMISSION")
                .description("new permission description")
                .build();

        MvcResult createPermissionResult = mockMvc.perform(post("/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .content(objectMapper.writeValueAsString(permissionRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        // Deserialize the JSON response into ApiResponse<UserResponse>
        ApiResponse<PermissionResponse> createPermissionApiResponse = objectMapper.readValue(createPermissionResult.getResponse().getContentAsByteArray(),
                new TypeReference<ApiResponse<PermissionResponse>>() {
                }
        );

        assertThat(createPermissionApiResponse).isNotNull();
        assertThat(createPermissionApiResponse.getCode()).isEqualTo(DEFAULT_CODE_RESPONSE);
        assertThat(createPermissionApiResponse.getResult()).isNotNull();
        assertThat(createPermissionApiResponse.getResult().getName()).isEqualTo(expectedPermissionResponse.getName());
        assertThat(createPermissionApiResponse.getResult().getDescription()).isEqualTo(expectedPermissionResponse.getDescription());

        // getAllPermissions method
        MvcResult getAllPermissionResult = mockMvc.perform(get("/permissions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<List<PermissionResponse>> getAllPermissionsApiResponse = objectMapper.readValue(getAllPermissionResult.getResponse().getContentAsByteArray(),
                new TypeReference<ApiResponse<List<PermissionResponse>>>() {
                }
        );

        assertThat(getAllPermissionsApiResponse).isNotNull();
        assertThat(getAllPermissionsApiResponse.getResult().size()).isEqualTo(2);

        PermissionResponse permissionCreated = getAllPermissionsApiResponse.getResult().getLast();
        assertThat(permissionCreated.getName()).isEqualTo(createPermissionApiResponse.getResult().getName());
        assertThat(permissionCreated.getDescription()).isEqualTo(createPermissionApiResponse.getResult().getDescription());

        // for deletePermission method
        ApiResponse<String> deletePermissionApiResponse = ApiResponse.<String>builder()
                .result(MessageResponse.SUCCESS)
                .build();
        mockMvc.perform(MockMvcRequestBuilders.delete("/permissions/" + permissionCreated.getName())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(deletePermissionApiResponse)))
                .andDo(print());

        // getAllPermissions method
        MvcResult getAllPermissionResult2 = mockMvc.perform(get("/permissions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<List<PermissionResponse>> getAllPermissionsApiResponse2 = objectMapper.readValue(
                getAllPermissionResult2.getResponse().getContentAsByteArray(),
                new TypeReference<ApiResponse<List<PermissionResponse>>>() {
                }
        );

        assertThat(getAllPermissionsApiResponse2).isNotNull();
        assertThat(getAllPermissionsApiResponse2.getResult().size()).isEqualTo(1);
    }

    @Test
    void createPermission_PermissionExisted() throws Exception {
        String authToken = login();
        PermissionRequest permissionRequest = PermissionRequest.builder()
                .name(permissionSaved.getName())
                .description("new permission description")
                .build();

        ApiResponse<String> createPermissionApiResponse = ApiResponse.<String>builder()
                .code(ErrorCode.PERMISSION_EXISTED.getCode())
                .message(ErrorCode.PERMISSION_EXISTED.getMessage())
                .build();
        mockMvc.perform(post("/permissions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(content().string(objectMapper.writeValueAsString(createPermissionApiResponse)));
    }
}
