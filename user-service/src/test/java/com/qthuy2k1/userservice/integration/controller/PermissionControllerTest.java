package com.qthuy2k1.userservice.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.userservice.UserApplication;
import com.qthuy2k1.userservice.dto.request.PermissionRequest;
import com.qthuy2k1.userservice.dto.response.ApiResponse;
import com.qthuy2k1.userservice.dto.response.MessageResponse;
import com.qthuy2k1.userservice.dto.response.PermissionResponse;
import com.qthuy2k1.userservice.enums.ErrorCode;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

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

        String createPermissionResponseBody = given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .body(objectMapper.writeValueAsString(permissionRequest))
                .when().post("/permissions")
                .then().statusCode(HttpStatus.CREATED.value())
                .extract().asString();

        // Deserialize the JSON response into ApiResponse<UserResponse>
        ApiResponse<PermissionResponse> createPermissionApiResponse = objectMapper.readValue(
                createPermissionResponseBody,
                new TypeReference<ApiResponse<PermissionResponse>>() {
                }
        );

        assertThat(createPermissionApiResponse).isNotNull();
        assertThat(createPermissionApiResponse.getCode()).isEqualTo(DEFAULT_CODE_RESPONSE);
        assertThat(createPermissionApiResponse.getResult()).isNotNull();
        assertThat(createPermissionApiResponse.getResult().getName()).isEqualTo(expectedPermissionResponse.getName());
        assertThat(createPermissionApiResponse.getResult().getDescription()).isEqualTo(expectedPermissionResponse.getDescription());

        // getAllPermissions method
        String getAllPermissionsResponseBody = given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().get("/permissions")
                .then().statusCode(HttpStatus.OK.value())
                .extract().asString();

        ApiResponse<List<PermissionResponse>> getAllPermissionsApiResponse = objectMapper.readValue(
                getAllPermissionsResponseBody,
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
        given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().delete("/permissions/" + permissionCreated.getName())
                .then().statusCode(HttpStatus.OK.value())
                .body(equalTo(objectMapper.writeValueAsString(deletePermissionApiResponse)));

        // getAllPermissions method
        String getAllPermissionsResponseBody2 = given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().get("/permissions")
                .then().statusCode(HttpStatus.OK.value())
                .extract().asString();

        ApiResponse<List<PermissionResponse>> getAllPermissionsApiResponse2 = objectMapper.readValue(
                getAllPermissionsResponseBody2,
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
        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .body(objectMapper.writeValueAsString(permissionRequest))
                .when().post("/permissions")
                .then().statusCode(HttpStatus.BAD_REQUEST.value())
                .body(equalTo(objectMapper.writeValueAsString(createPermissionApiResponse)));
    }
}
