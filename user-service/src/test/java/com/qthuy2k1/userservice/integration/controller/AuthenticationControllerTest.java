package com.qthuy2k1.userservice.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.qthuy2k1.userservice.UserApplication;
import com.qthuy2k1.userservice.dto.request.AuthenticationRequest;
import com.qthuy2k1.userservice.dto.response.ApiResponse;
import com.qthuy2k1.userservice.dto.response.AuthenticationResponse;
import com.qthuy2k1.userservice.dto.response.IntrospectResponse;
import com.qthuy2k1.userservice.dto.response.MessageResponse;
import com.qthuy2k1.userservice.enums.ErrorCode;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
public class AuthenticationControllerTest extends BaseControllerTest {
    @Test
    void authenticate() throws Exception {
        String authToken = login();
        assertThat(authToken).isNotBlank();
    }

    @Test
    void authentication_UserNotFound() throws Exception {
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .email("notfound" + userSaved1.getEmail())
                .password(USER_PASSWORD)
                .build();

        ApiResponse<AuthenticationResponse> apiResponse = ApiResponse.<AuthenticationResponse>builder()
                .code(ErrorCode.USER_NOT_FOUND.getCode())
                .message(ErrorCode.USER_NOT_FOUND.getMessage())
                .build();
        given()
                .contentType(ContentType.JSON).body(objectMapper.writeValueAsString(authenticationRequest))
                .when().post("/auth/token")
                .then().statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void authentication_Unauthenticated() throws Exception {
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .email(userSaved1.getEmail())
                .password(USER_PASSWORD + "123")
                .build();

        ApiResponse<AuthenticationResponse> apiResponse = ApiResponse.<AuthenticationResponse>builder()
                .code(ErrorCode.UNAUTHENTICATED.getCode())
                .message(ErrorCode.UNAUTHENTICATED.getMessage())
                .build();
        given()
                .contentType(ContentType.JSON).body(objectMapper.writeValueAsString(authenticationRequest))
                .when().post("/auth/token")
                .then().statusCode(HttpStatus.UNAUTHORIZED.value()).body(equalTo(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void introspect() throws Exception {
        String authToken = login();
        String introspectResponseBody = given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().post("/auth/introspect")
                .then().statusCode(HttpStatus.OK.value())
                .extract().asString();
        ApiResponse<IntrospectResponse> introspectResponse = objectMapper.readValue(
                introspectResponseBody,
                new TypeReference<ApiResponse<IntrospectResponse>>() {
                }
        );

        assertThat(introspectResponse).isNotNull();
        assertThat(introspectResponse.getCode()).isEqualTo(DEFAULT_CODE_RESPONSE);
        assertThat(introspectResponse.getResult().isValid()).isTrue();
    }

    @Test
    void logout() throws Exception {
        String authToken = login();
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .code(DEFAULT_CODE_RESPONSE)
                .result(MessageResponse.SUCCESS)
                .build();
        given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().post("/auth/logout")
                .then().statusCode(HttpStatus.OK.value())
                .body(equalTo(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void refreshToken() throws Exception {
        String authToken = login();
        String refreshTokenResponseBody = given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when().post("/auth/refresh-token")
                .then().statusCode(HttpStatus.OK.value())
                .extract().asString();
        ApiResponse<AuthenticationResponse> authenticationResponse = objectMapper.readValue(
                refreshTokenResponseBody,
                new TypeReference<ApiResponse<AuthenticationResponse>>() {
                }
        );

        assertThat(authenticationResponse).isNotNull();
        assertThat(authenticationResponse.getCode()).isEqualTo(DEFAULT_CODE_RESPONSE);
        assertThat(authenticationResponse.getResult().isAuthenticated()).isTrue();
        String refreshToken = authenticationResponse.getResult().getToken();
        assertThat(refreshToken).isNotBlank();

        ApiResponse<IntrospectResponse> introspectResponse = ApiResponse.<IntrospectResponse>builder()
                .code(DEFAULT_CODE_RESPONSE)
                .result(IntrospectResponse.builder()
                        .valid(true)
                        .build())
                .build();
        given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken)
                .contentType(ContentType.JSON)
                .when().post("/auth/introspect")
                .then().statusCode(HttpStatus.OK.value())
                .body(equalTo(objectMapper.writeValueAsString(introspectResponse)));
    }
}
