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
    UNAUTHORIZED(1009, "access denied", HttpStatus.FORBIDDEN),
    FEIGN_ERROR(1010, "client feign error", HttpStatus.INTERNAL_SERVER_ERROR),
    START_DATE_NULL(1011, "start date must not be null", HttpStatus.BAD_REQUEST),
    END_DATE_NULL(1012, "end date must not be null", HttpStatus.NOT_FOUND),
    SERVICE_UNAVAILABLE(1013, "Order service is currently unavailable. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_STATUS_LABEL(1014, "Invalid status label", HttpStatus.BAD_REQUEST);

    int code;
    String message;
    HttpStatusCode statusCode;
}
