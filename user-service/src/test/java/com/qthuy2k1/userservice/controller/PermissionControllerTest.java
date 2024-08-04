package com.qthuy2k1.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.userservice.dto.request.PermissionRequest;
import com.qthuy2k1.userservice.dto.response.ApiResponse;
import com.qthuy2k1.userservice.dto.response.MessageResponse;
import com.qthuy2k1.userservice.dto.response.PermissionResponse;
import com.qthuy2k1.userservice.service.PermissionService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PermissionController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class PermissionControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private PermissionService permissionService;
    @InjectMocks
    private PermissionController permissionController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void create() throws Exception {
        // given
        PermissionRequest permissionRequest = PermissionRequest.builder()
                .name("READ_DATA")
                .description("read data description")
                .build();
        PermissionResponse permissionResponse = PermissionResponse.builder()
                .name("READ_DATA")
                .description("read data description")
                .build();

        given(permissionService.create(any())).willReturn(permissionResponse);

        // when
        ApiResponse<PermissionResponse> apiResponse = ApiResponse.<PermissionResponse>builder()
                .result(permissionResponse)
                .build();

        mockMvc.perform(post("/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(permissionService).should().create(any());
    }

    @Test
    void getAll() throws Exception {
        // given
        PermissionResponse permissionResponse = PermissionResponse.builder()
                .name("READ_DATA")
                .description("read data description")
                .build();

        List<PermissionResponse> permissions = new ArrayList<>();
        permissions.add(permissionResponse);

        given(permissionService.getAll()).willReturn(permissions);

        // when
        ApiResponse<List<PermissionResponse>> apiResponse = ApiResponse.<List<PermissionResponse>>builder()
                .result(permissions)
                .build();

        mockMvc.perform(get("/permissions"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)));

        // then
        then(permissionService).should().getAll();
    }

    @Test
    void delete() throws Exception {
        // given
        String name = "READ_DATA";

        // when
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .result(MessageResponse.SUCCESS)
                .build();
        mockMvc.perform(MockMvcRequestBuilders.delete("/permissions/" + name)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(permissionService).should().delete(name);
    }
}
