package com.qthuy2k1.userservice.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.userservice.UserApplication;
import com.qthuy2k1.userservice.dto.request.RoleRequest;
import com.qthuy2k1.userservice.dto.response.ApiResponse;
import com.qthuy2k1.userservice.dto.response.MessageResponse;
import com.qthuy2k1.userservice.dto.response.RoleResponse;
import com.qthuy2k1.userservice.enums.ErrorCode;
import com.qthuy2k1.userservice.mapper.PermissionMapper;
import com.qthuy2k1.userservice.repository.UserRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;

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
public class RoleControllerTest extends BaseControllerTest {
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

        String createRoleResponseBody = given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .body(objectMapper.writeValueAsString(roleRequest))
                .when().post("/roles")
                .then().statusCode(HttpStatus.CREATED.value())
                .extract().asString();

        // Deserialize the JSON response into ApiResponse<RoleResponse>
        ApiResponse<RoleResponse> createRoleApiResponse = objectMapper.readValue(
                createRoleResponseBody,
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
        String getAllRolesResponseBody = given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().get("/roles")
                .then().statusCode(HttpStatus.OK.value())
                .extract().asString();

        ApiResponse<List<RoleResponse>> getAllRolesApiResponse = objectMapper.readValue(
                getAllRolesResponseBody,
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
        given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().delete("/roles/" + roleCreated.getName())
                .then().statusCode(HttpStatus.OK.value())
                .body(equalTo(objectMapper.writeValueAsString(deleteRoleApiResponse)));

        // for getAllRoles method
        String getAllRolesResponseBody2 = given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().get("/roles")
                .then().statusCode(HttpStatus.OK.value())
                .extract().asString();

        ApiResponse<List<RoleResponse>> getAllRolesApiResponse2 = objectMapper.readValue(
                getAllRolesResponseBody2,
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
        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .body(objectMapper.writeValueAsString(roleRequest))
                .when().post("/roles")
                .then().statusCode(HttpStatus.BAD_REQUEST.value())
                .body(equalTo(objectMapper.writeValueAsString(createRoleApiResponse)));
    }

}
