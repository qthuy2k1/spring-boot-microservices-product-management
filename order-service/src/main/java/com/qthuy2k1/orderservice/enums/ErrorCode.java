package com.qthuy2k1.orderservice.enums;

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
    INVALID_KEY(1001, "Invalid message key", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND(1002, "product not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(1003, "user not found", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND(1004, "order not found", HttpStatus.NOT_FOUND),
    PRODUCT_OUT_OF_STOCK(1005, "product out of stock", HttpStatus.NOT_FOUND),
    PRODUCT_CATEGORY_NOT_FOUND(1006, "product category not found", HttpStatus.NOT_FOUND),
    INVALID_ID(1007, "invalid id", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1008, "unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1009, "you do not have permission", HttpStatus.FORBIDDEN),
    FEIGN_ERROR(1010, "client feign error", HttpStatus.INTERNAL_SERVER_ERROR);

    int code;
    String message;
    HttpStatusCode statusCode;
}
