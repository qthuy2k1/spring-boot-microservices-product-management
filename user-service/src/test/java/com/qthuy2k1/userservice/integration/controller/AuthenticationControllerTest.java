package com.qthuy2k1.userservice.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.qthuy2k1.userservice.UserApplication;
import com.qthuy2k1.userservice.dto.request.AuthenticationRequest;
import com.qthuy2k1.userservice.dto.response.ApiResponse;
import com.qthuy2k1.userservice.dto.response.AuthenticationResponse;
import com.qthuy2k1.userservice.dto.response.IntrospectResponse;
import com.qthuy2k1.userservice.dto.response.MessageResponse;
import com.qthuy2k1.userservice.enums.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andReturn();
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
        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andReturn();
    }

    @Test
    void introspect() throws Exception {
        String authToken = login();
        MvcResult introspectResult = mockMvc.perform(post("/auth/introspect")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<IntrospectResponse> introspectResponse = objectMapper.readValue(
                introspectResult.getResponse().getContentAsByteArray(),
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
        mockMvc.perform(post("/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void refreshToken() throws Exception {
        String authToken = login();
        MvcResult refreshTokenResult = mockMvc.perform(post("/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<AuthenticationResponse> authenticationResponse = objectMapper.readValue(
                refreshTokenResult.getResponse().getContentAsByteArray(),
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
        mockMvc.perform(post("/auth/introspect")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(introspectResponse)))
                .andReturn();
    }
}
