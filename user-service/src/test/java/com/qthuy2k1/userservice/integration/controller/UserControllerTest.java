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
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

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

        UserResponse expectedUserResponse = UserResponse.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .roles(Set.of(roleMapper.toRoleResponse(userRoleSaved)))
                .build();

        String createUserResponseBody = given()
                .contentType(ContentType.JSON).body(objectMapper.writeValueAsString(userRequest))
                .when().post("/users/register")
                .then().statusCode(HttpStatus.CREATED.value())
                .extract().asString();

        // Deserialize the JSON response into ApiResponse<UserResponse>
        ApiResponse<UserResponse> createUserApiResponse = objectMapper.readValue(
                createUserResponseBody,
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

        String getAllUsersResponseBody = given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().get("/users")
                .then().statusCode(HttpStatus.OK.value())
                .extract().asString();

        ApiResponse<List<UserResponse>> getAllUsersApiResponse = objectMapper.readValue(
                getAllUsersResponseBody,
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
        String getAllUsersResponseBody = given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().get("/users")
                .then().statusCode(HttpStatus.OK.value())
                .extract().asString();

        ApiResponse<List<UserResponse>> getAllUsersApiResponse = objectMapper.readValue(
                getAllUsersResponseBody,
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
        given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().get("/users")
                .then().statusCode(HttpStatus.FORBIDDEN.value())
                .assertThat().body(equalTo(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void signup_ExceptionThrown_InvalidRequest() throws Exception {
        UserRequest userRequest = UserRequest.builder()
                .name("  ") // only whitespaces
                .email("doe@gmail.com") // invalid email address
                .password("123123")
                .build();

        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .code(ErrorCode.USERNAME_BLANK.getCode())
                .message(ErrorCode.USERNAME_BLANK.getMessage())
                .build();
        given()
                .contentType(ContentType.JSON).body(objectMapper.writeValueAsString(userRequest))
                .when().post("/users/register")
                .then().statusCode(HttpStatus.BAD_REQUEST.value())
                .body(equalTo(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void deleteUserById() throws Exception {
        String authToken = login();
        int id = userSaved1.getId();

        ApiResponse<String> deleteUserApiResponse = ApiResponse.<String>builder()
                .result(MessageResponse.SUCCESS)
                .build();
        given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().delete("/users/" + id)
                .then().statusCode(HttpStatus.OK.value())
                .body(equalTo(objectMapper.writeValueAsString(deleteUserApiResponse)));

        ApiResponse<UserResponse> getUserApiResponse = ApiResponse.<UserResponse>builder()
                .code(ErrorCode.USER_NOT_FOUND.getCode())
                .message(ErrorCode.USER_NOT_FOUND.getMessage())
                .build();
        given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().get("/users/" + id)
                .then().statusCode(HttpStatus.NOT_FOUND.value())
                .assertThat().body(equalTo(objectMapper.writeValueAsString(getUserApiResponse)));
    }

    @Test
    void deleteUserById_UserNotFound() throws Exception {
        String authToken = login();
        int id = userSaved2.getId() + 1;

        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .code(ErrorCode.USER_NOT_FOUND.getCode())
                .message(ErrorCode.USER_NOT_FOUND.getMessage())
                .build();
        given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().delete("/users/" + id)
                .then().statusCode(HttpStatus.NOT_FOUND.value())
                .assertThat().body(equalTo(objectMapper.writeValueAsString(apiResponse)));
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

        UserResponse expectedUserResponse = UserResponse.builder()
                .id(userSaved1.getId())
                .name("John Doe")
                .email("doe@gmail.com")
                .roles(Set.of(roleMapper.toRoleResponse(userRoleSaved)))
                .build();

        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .result(expectedUserResponse)
                .build();
        given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON).body(objectMapper.writeValueAsString(userRequest))
                .when().put("/users/" + id)
                .then().statusCode(HttpStatus.OK.value())
                .assertThat().body(equalTo(objectMapper.writeValueAsString(apiResponse)));
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

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(ErrorCode.USERNAME_BLANK.getCode())
                .message(ErrorCode.USERNAME_BLANK.getMessage())
                .build();
        given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON).body(objectMapper.writeValueAsString(userRequest))
                .when().put("/users/" + id)
                .then().statusCode(HttpStatus.BAD_REQUEST.value())
                .assertThat().body(equalTo(objectMapper.writeValueAsString(apiResponse)));
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
        String userResponseBody = given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().get("/users/" + id)
                .then().statusCode(HttpStatus.OK.value()).extract().asString();

        // Deserialize the JSON response into ApiResponse<UserResponse>
        ApiResponse<UserResponse> getUserResponse = objectMapper.readValue(
                userResponseBody,
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
        given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().get("/users/" + id)
                .then().statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void getUserById_ExceptionThrown_Unauthorized() throws Exception {
        String authToken = login(userSaved2); // not have admin role
        int id = userSaved2.getId();

        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .code(ErrorCode.UNAUTHORIZED.getCode())
                .message(ErrorCode.UNAUTHORIZED.getMessage())
                .build();

        given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().get("/users/" + id)
                .then().statusCode(HttpStatus.FORBIDDEN.value())
                .body(equalTo(objectMapper.writeValueAsString(apiResponse)));
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

        given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().get("/users/my-info")
                .then().statusCode(HttpStatus.OK.value())
                .body(equalTo(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void existsById() throws Exception {
        String authToken = login();
        given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().get("/users/" + userSaved2.getId() + "/is-exists")
                .then().statusCode(HttpStatus.OK.value())
                .body(equalTo("true"));
    }

    @Test
    void existsById_UserNotFound() throws Exception {
        String authToken = login();
        given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().get("/users/" + userSaved2.getId() + 1 + "/is-exists")
                .then().statusCode(HttpStatus.OK.value())
                .body(equalTo("false"));
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
        given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().get("/users/email/" + userSaved1.getEmail())
                .then().statusCode(HttpStatus.OK.value())
                .body(equalTo(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void getUserByEmail_ExceptionThrown_UserNotFound() throws Exception {
        String authToken = login();

        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .code(ErrorCode.USER_NOT_FOUND.getCode())
                .message(ErrorCode.USER_NOT_FOUND.getMessage())
                .build();
        given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().get("/users/email/" + userSaved1.getEmail() + "notfound")
                .then().statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo(objectMapper.writeValueAsString(apiResponse)));
    }
}