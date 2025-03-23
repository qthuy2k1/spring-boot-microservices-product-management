package com.qthuy2k1.userservice.enums;

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
    UNCATEGORIZED_EXCEPTION(9999, "unknown error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "invalid message key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "user existed", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1003, "user not found", HttpStatus.NOT_FOUND),
    INVALID_ID(1004, "invalid id", HttpStatus.BAD_REQUEST),
    USERNAME_NULL(1005, "your name shouldn't be null", HttpStatus.BAD_REQUEST),
    USERNAME_BLANK(1006, "your name shouldn't be blank", HttpStatus.BAD_REQUEST),
    PASSWORD_MIN(1007, "your password must contains at least 6 characters", HttpStatus.BAD_REQUEST),
    PASSWORD_NULL(1008, "your password shouldn't be null", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1009, "invalid email address", HttpStatus.BAD_REQUEST),
    EMAIL_BLANK(1010, "your email address shouldn't be blank", HttpStatus.BAD_REQUEST),
    EMAIL_NULL(1011, "your email address shouldn't be null", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1012, "unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1013, "you do not have permission", HttpStatus.FORBIDDEN),
    ROLE_NOT_FOUND(1014, "role not found", HttpStatus.BAD_REQUEST),
    BAD_CREDENTIALS(1015, "wrong email or password", HttpStatus.UNAUTHORIZED),
    PERMISSION_NOT_FOUND(1016, "permission not found", HttpStatus.BAD_REQUEST),
    PERMISSION_EXISTED(1017, "permission existed", HttpStatus.BAD_REQUEST),
    ROLE_EXISTED(1018, "role existed", HttpStatus.BAD_REQUEST);

    int code;
    String message;
    HttpStatusCode statusCode;
}
