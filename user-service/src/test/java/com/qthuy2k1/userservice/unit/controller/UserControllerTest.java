package com.qthuy2k1.userservice.unit.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.userservice.controller.UserController;
import com.qthuy2k1.userservice.dto.request.UserRequest;
import com.qthuy2k1.userservice.dto.request.UserUpdateRequest;
import com.qthuy2k1.userservice.dto.response.*;
import com.qthuy2k1.userservice.enums.ErrorCode;
import com.qthuy2k1.userservice.exception.AppException;
import com.qthuy2k1.userservice.service.UserService;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;
    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setup() {
        //Init MockMvc Object and build
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void signup() throws Exception {
        // given
        UserRequest userRequest = UserRequest.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .build();
        String userRequestString = objectMapper.writeValueAsString(userRequest);

        UserResponse userResponse = UserResponse.builder()
                .id(1)
                .name("John Doe")
                .email("doe@gmail.com")
                .roles(Set.of())
                .build();

        given(userService.createUser(any())).willReturn(userResponse);

        // when
        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .result(userResponse)
                .build();
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestString))
                .andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(userService).should().createUser(any());
    }

    @Test
    void signup_ExceptionThrown_InvalidRequest() throws Exception {
        // given
        UserRequest userRequest = UserRequest.builder()
                .name("  ") // only whitespaces
                .email("doe@gmail.com") // invalid email address
                .password("123123")
                .build();
        String userRequestString = objectMapper.writeValueAsString(userRequest);

        // when
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(ErrorCode.USERNAME_BLANK.getCode())
                .message(ErrorCode.USERNAME_BLANK.getMessage())
                .build();
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestString))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(userService).should(never()).createUser(any());
    }

    @Test
    void getAllUsers() throws Exception {
        // given
        List<UserResponse> users = new ArrayList<>();
        PermissionResponse permissionResponse = PermissionResponse.builder()
                .name("READ_DATA")
                .description("read data description")
                .build();
        RoleResponse roleResponse = RoleResponse.builder()
                .name("USER")
                .description("user description")
                .permissions(Set.of(permissionResponse))
                .build();
        users.add(
                UserResponse.builder()
                        .id(1)
                        .name("John Doe")
                        .email("johndoe@gmail.com")
                        .roles(Set.of(roleResponse))
                        .build()
        );
        users.add(
                UserResponse.builder()
                        .id(2)
                        .name("Jane Doe")
                        .email("janedoe@gmail.com")
                        .roles(Set.of(roleResponse))
                        .build()
        );
        given(userService.getAllUsers()).willReturn(users);

        // when
        ApiResponse<List<UserResponse>> apiResponse = ApiResponse.<List<UserResponse>>builder()
                .result(users)
                .build();
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)));

        // then
        then(userService).should().getAllUsers();
    }

    @Test
    void deleteUserById() throws Exception {
        // given
        int id = 1;

        // when
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .result(MessageResponse.SUCCESS)
                .build();
        mockMvc.perform(delete("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(userService).should().deleteUserById(id);
    }


    @Test
    void updateUserById() throws Exception {
        // given
        int id = 1;
        UserUpdateRequest userRequest = UserUpdateRequest.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .build();
        String userRequestString = objectMapper.writeValueAsString(userRequest);

        UserResponse userResponse = UserResponse.builder()
                .id(1)
                .name("John Doe")
                .email("doe@gmail.com")
                .roles(Set.of())
                .build();

        given(userService.updateUserById(any(), any())).willReturn(userResponse);

        // when
        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .result(userResponse)
                .build();
        mockMvc.perform(put("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestString))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(userService).should().updateUserById(any(), any());
    }

    @Test
    void updateUserById_ExceptionThrown_InvalidRequest() throws Exception {
        // given
        int id = 1;
        UserUpdateRequest userRequest = UserUpdateRequest.builder()
                .name("  ") // only whitespaces
                .email("doe@gmail.com") // invalid email address
                .password("123123")
                .build();
        String userRequestString = objectMapper.writeValueAsString(userRequest);

        // when
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(ErrorCode.USERNAME_BLANK.getCode())
                .message(ErrorCode.USERNAME_BLANK.getMessage())
                .build();
        mockMvc.perform(put("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestString))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(userService).should(never()).updateUserById(any(), any());
    }

    @Test
    void getUserById() throws Exception {
        // given
        int id = 1;
        UserResponse userResponse = UserResponse.builder()
                .id(1)
                .name("John Doe")
                .email("doe@gmail.com")
                .build();

        given(userService.getUserById(id)).willReturn(userResponse);

        // when
        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .result(userResponse)
                .build();
        mockMvc.perform(get("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(userService).should().getUserById(id);
    }

    @Test
    void getUserById_ExceptionThrown_UserNotFound() throws Exception {
        // given
        int id = 1;

        given(userService.getUserById(id)).willThrow(new AppException(ErrorCode.USER_NOT_FOUND));

        // when
        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .code(ErrorCode.USER_NOT_FOUND.getCode())
                .message(ErrorCode.USER_NOT_FOUND.getMessage())
                .build();
        mockMvc.perform(get("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(userService).should().getUserById(id);
    }
}