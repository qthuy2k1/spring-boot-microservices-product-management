package com.qthuy2k1.userservice.controller;

import com.nimbusds.jose.JOSEException;
import com.qthuy2k1.userservice.dto.request.AuthenticationRequest;
import com.qthuy2k1.userservice.dto.response.ApiResponse;
import com.qthuy2k1.userservice.dto.response.AuthenticationResponse;
import com.qthuy2k1.userservice.dto.response.IntrospectResponse;
import com.qthuy2k1.userservice.dto.response.MessageResponse;
import com.qthuy2k1.userservice.service.IAuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    IAuthenticationService authenticationService;

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader)
            throws ParseException, JOSEException {
        return ApiResponse.<IntrospectResponse>builder()
                .result(authenticationService.introspect(authorizationHeader))
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<String> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) throws ParseException, JOSEException {
        authenticationService.logout(authorizationHeader);
        return ApiResponse.<String>builder()
                .result(MessageResponse.SUCCESS)
                .build();
    }

    @PostMapping("/refresh-token")
    ApiResponse<AuthenticationResponse> refreshToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader)
            throws ParseException, JOSEException {
        AuthenticationResponse authenticationResponse = authenticationService.refreshToken(authorizationHeader);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationResponse)
                .build();
    }
}
