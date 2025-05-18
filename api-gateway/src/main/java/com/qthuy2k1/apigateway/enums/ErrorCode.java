package com.qthuy2k1.apigateway.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum ErrorCode {
    UNAUTHENTICATED_MISSING_AUTHORIZATION_HEADER(1001, "unauthenticated: no Authorization header found", HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED_INVALID_TOKEN(1002, "unauthenticated: token is invalid", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1003, "access denied", HttpStatus.FORBIDDEN);
    int code;
    String message;
    HttpStatusCode statusCode;
}
