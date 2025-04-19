package com.qthuy2k1.userservice.integration.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.qthuy2k1.userservice.UserApplication;
import com.qthuy2k1.userservice.dto.request.UserRequest;
import com.qthuy2k1.userservice.dto.request.UserUpdateRequest;
import com.qthuy2k1.userservice.dto.response.ApiResponse;
import com.qthuy2k1.userservice.dto.response.MessageResponse;
import com.qthuy2k1.userservice.dto.response.RoleResponse;
import com.qthuy2k1.userservice.dto.response.UserResponse;
import com.qthuy2k1.userservice.enums.ErrorCode;
import com.qthuy2k1.userservice.mapper.RoleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
class UserControllerTest extends BaseControllerTest {
    private final RoleMapper roleMapper = Mappers.getMapper(RoleMapper.class);

    @Test
    void signup_And_GetAll() throws Exception {
        UserRequest userRequest = UserRequest.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .build();
        String userRequestString = objectMapper.writeValueAsString(userRequest);

        UserResponse expectedUserResponse = UserResponse.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .roles(Set.of(roleMapper.toRoleResponse(userRoleSaved)))
                .build();

        MvcResult createUserResult = mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestString))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        // Deserialize the JSON response into ApiResponse<UserResponse>
        ApiResponse<UserResponse> createUserApiResponse = objectMapper.readValue(
                createUserResult.getResponse().getContentAsByteArray(),
                new TypeReference<ApiResponse<UserResponse>>() {
                }
        );

        assertThat(createUserApiResponse).isNotNull();
        assertThat(createUserApiResponse.getCode()).isEqualTo(DEFAULT_CODE_RESPONSE);
        assertThat(createUserApiResponse.getResult()).isNotNull();
        assertThat(createUserApiResponse.getResult().getName()).isEqualTo(expectedUserResponse.getName());
        assertThat(createUserApiResponse.getResult().getEmail()).isEqualTo(expectedUserResponse.getEmail());
        assertThat(createUserApiResponse.getResult().getRoles()).isEqualTo(expectedUserResponse.getRoles());

        String authToken = login();
        MvcResult getAllUsersResult = mockMvc.perform(get("/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<List<UserResponse>> getAllUsersApiResponse = objectMapper.readValue(
                getAllUsersResult.getResponse().getContentAsByteArray(),
                new TypeReference<ApiResponse<List<UserResponse>>>() {
                }
        );

        assertThat(getAllUsersApiResponse).isNotNull();
        assertThat(getAllUsersApiResponse.getResult().size()).isEqualTo(3);

        UserResponse userCreated = getAllUsersApiResponse.getResult().getLast();
        assertThat(userCreated.getName()).isEqualTo(createUserApiResponse.getResult().getName());
        assertThat(userCreated.getEmail()).isEqualTo(createUserApiResponse.getResult().getEmail());
        assertThat(userCreated.getRoles()).isEqualTo(createUserApiResponse.getResult().getRoles());
    }

    @Test
    void getAllUsers() throws Exception {
        String authToken = login();
        MvcResult getAllUsersResult = mockMvc.perform(get("/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<List<UserResponse>> getAllUsersApiResponse = objectMapper.readValue(
                getAllUsersResult.getResponse().getContentAsByteArray(),
                new TypeReference<ApiResponse<List<UserResponse>>>() {
                }
        );

        assertThat(getAllUsersApiResponse).isNotNull();
        assertThat(getAllUsersApiResponse.getResult().size()).isEqualTo(2);

        UserResponse userCreated = getAllUsersApiResponse.getResult().getFirst();
        assertThat(userCreated.getName()).isEqualTo(userSaved1.getName());
        assertThat(userCreated.getEmail()).isEqualTo(userSaved1.getEmail());
        Set<RoleResponse> userSaved1RoleResponse = userSaved1.getRoles().stream().map(roleMapper::toRoleResponse).collect(Collectors.toSet());
        assertThat(userCreated.getRoles()).isEqualTo(userSaved1RoleResponse);
    }

    @Test
    void getAllUsers_Unauthorized() throws Exception {
        String authToken = login(userSaved2);
        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .code(ErrorCode.UNAUTHORIZED.getCode())
                .message(ErrorCode.UNAUTHORIZED.getMessage())
                .build();
        mockMvc.perform(get("/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void signup_ExceptionThrown_InvalidRequest() throws Exception {
        UserRequest userRequest = UserRequest.builder()
                .name("  ") // only whitespaces
                .email("doe@gmail.com") // invalid email address
                .password("123123")
                .build();
        String userRequestString = objectMapper.writeValueAsString(userRequest);

        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .code(ErrorCode.USERNAME_BLANK.getCode())
                .message(ErrorCode.USERNAME_BLANK.getMessage())
                .build();
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestString))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andReturn();
    }

    @Test
    void deleteUserById() throws Exception {
        String authToken = login();
        int id = userSaved1.getId();

        ApiResponse<String> deleteUserApiResponse = ApiResponse.<String>builder()
                .result(MessageResponse.SUCCESS)
                .build();
        mockMvc.perform(delete("/users/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(deleteUserApiResponse)));


        ApiResponse<UserResponse> getUserApiResponse = ApiResponse.<UserResponse>builder()
                .code(ErrorCode.USER_NOT_FOUND.getCode())
                .message(ErrorCode.USER_NOT_FOUND.getMessage())
                .build();
        mockMvc.perform(get("/users/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(objectMapper.writeValueAsString(getUserApiResponse)));
    }

    @Test
    void deleteUserById_UserNotFound() throws Exception {
        String authToken = login();
        int id = userSaved2.getId() + 1;

        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .code(ErrorCode.USER_NOT_FOUND.getCode())
                .message(ErrorCode.USER_NOT_FOUND.getMessage())
                .build();
        mockMvc.perform(delete("/users/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)));
    }


    @Test
    void updateUserById() throws Exception {
        String authToken = login();
        int id = userSaved1.getId();
        UserUpdateRequest userRequest = UserUpdateRequest.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password(USER_PASSWORD)
                .roles(List.of(userRoleSaved.getName()))
                .build();
        String userRequestString = objectMapper.writeValueAsString(userRequest);

        UserResponse expectedUserResponse = UserResponse.builder()
                .id(userSaved1.getId())
                .name("John Doe")
                .email("doe@gmail.com")
                .roles(Set.of(roleMapper.toRoleResponse(userRoleSaved)))
                .build();

        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .result(expectedUserResponse)
                .build();
        mockMvc.perform(put("/users/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestString))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void updateUserById_ExceptionThrown_InvalidRequest() throws Exception {
        String authToken = login();
        int id = userSaved1.getId();
        UserUpdateRequest userRequest = UserUpdateRequest.builder()
                .name("  ") // only whitespaces
                .email("doe@gmail.com") // invalid email address
                .password(USER_PASSWORD)
                .build();
        String userRequestString = objectMapper.writeValueAsString(userRequest);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(ErrorCode.USERNAME_BLANK.getCode())
                .message(ErrorCode.USERNAME_BLANK.getMessage())
                .build();
        mockMvc.perform(put("/users/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestString))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void getUserById() throws Exception {
        String authToken = login();
        int id = userSaved1.getId();
        Set<RoleResponse> userRoles = userSaved1.getRoles().stream().map(roleMapper::toRoleResponse).collect(Collectors.toSet());
        UserResponse expectedUserResponse = UserResponse.builder()
                .id(userSaved1.getId())
                .name(userSaved1.getName())
                .email(userSaved1.getEmail())
                .roles(userRoles)
                .build();

        ApiResponse<UserResponse> expectedApiResponse = ApiResponse.<UserResponse>builder()
                .result(expectedUserResponse)
                .build();
        MvcResult result = mockMvc.perform(get("/users/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Deserialize the JSON response into ApiResponse<UserResponse>
        ApiResponse<UserResponse> getUserResponse = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                new TypeReference<ApiResponse<UserResponse>>() {
                }
        );

        assertThat(getUserResponse).isNotNull();
        assertThat(expectedApiResponse.getCode()).isEqualTo(getUserResponse.getCode());
        assertThat(expectedApiResponse.getResult().getName()).isEqualTo(getUserResponse.getResult().getName());
        assertThat(expectedApiResponse.getResult().getEmail()).isEqualTo(getUserResponse.getResult().getEmail());
        assertThat(expectedApiResponse.getResult().getRoles()).isEqualTo(getUserResponse.getResult().getRoles());
    }

    @Test
    void getUserById_ExceptionThrown_UserNotFound() throws Exception {
        String authToken = login();

        int id = userSaved2.getId() + 1;

        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .code(ErrorCode.USER_NOT_FOUND.getCode())
                .message(ErrorCode.USER_NOT_FOUND.getMessage())
                .build();
        mockMvc.perform(get("/users/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void getUserById_ExceptionThrown_Unauthorized() throws Exception {
        String authToken = login(userSaved2); // not have admin role

        int id = userSaved2.getId();

        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .code(ErrorCode.UNAUTHORIZED.getCode())
                .message(ErrorCode.UNAUTHORIZED.getMessage())
                .build();
        mockMvc.perform(get("/users/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void getMyInfo() throws Exception {
        String authToken = login(userSaved2); // not have admin role
        Set<RoleResponse> userRoles = userSaved2.getRoles().stream().map(roleMapper::toRoleResponse).collect(Collectors.toSet());
        UserResponse expectedUserResponse = UserResponse.builder()
                .id(userSaved2.getId())
                .name(userSaved2.getName())
                .email(userSaved2.getEmail())
                .roles(userRoles)
                .build();

        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .result(expectedUserResponse)
                .build();
        mockMvc.perform(get("/users/my-info")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void existsById() throws Exception {
        String authToken = login();
        mockMvc.perform(get("/users/" + userSaved2.getId() + "/is-exists")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void existsById_UserNotFound() throws Exception {
        String authToken = login();
        mockMvc.perform(get("/users/" + userSaved2.getId() + 1 + "/is-exists")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void getUserByEmail() throws Exception {
        String authToken = login();
        Set<RoleResponse> userRoles = userSaved1.getRoles().stream().map(roleMapper::toRoleResponse).collect(Collectors.toSet());
        UserResponse expectedUserResponse = UserResponse.builder()
                .id(userSaved1.getId())
                .name(userSaved1.getName())
                .email(userSaved1.getEmail())
                .roles(userRoles)
                .build();

        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .result(expectedUserResponse)
                .build();
        mockMvc.perform(get("/users/email/" + userSaved1.getEmail())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void getUserByEmail_ExceptionThrown_UserNotFound() throws Exception {
        String authToken = login();

        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .code(ErrorCode.USER_NOT_FOUND.getCode())
                .message(ErrorCode.USER_NOT_FOUND.getMessage())
                .build();
        mockMvc.perform(get("/users/email/" + userSaved1.getEmail() + "notfound")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)));
    }
}