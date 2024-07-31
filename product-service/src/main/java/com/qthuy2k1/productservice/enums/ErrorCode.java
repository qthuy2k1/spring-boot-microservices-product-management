package com.qthuy2k1.productservice.enums;

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
    PRODUCT_EXISTED(1002, "product existed", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND(1003, "product not found", HttpStatus.NOT_FOUND),
    INVALID_ID(1004, "invalid id", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1005, "unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1006, "you do not have permission", HttpStatus.FORBIDDEN),
    FEIGN_ERROR(1007, "client feign error", HttpStatus.INTERNAL_SERVER_ERROR),
    PRODUCT_CATEGORY_NOT_FOUND(1008, "product category not found", HttpStatus.NOT_FOUND),
    PRODUCT_NAME_BLANK(1009, "product name shouldn't be blank", HttpStatus.BAD_REQUEST),
    PRODUCT_DESCRIPTION_BLANK(1010, "product description shouldn't be blank", HttpStatus.BAD_REQUEST),
    PRODUCT_PRICE_MIN(1011, "product price must be greater than or equal 0", HttpStatus.BAD_REQUEST),
    PRODUCT_SKUCODE_BLANK(1012, "the skuCode shouldn't be blank", HttpStatus.BAD_REQUEST),
    PRODUCT_PRICE_NULL(1013, "product price shouldn't be null", HttpStatus.BAD_REQUEST),
    PRODUCT_CATEGORY_ID_MIN(1014, "product category id should be greater than 0", HttpStatus.BAD_REQUEST),
    PRODUCT_QUANTITY_MIN(1015, "product quantity should be greater than 0 or equal 0", HttpStatus.BAD_REQUEST),
    PRODUCT_ID_MIN(1016, "product id should be greater than 0", HttpStatus.BAD_REQUEST),
    ;

    int code;
    String message;
    HttpStatusCode statusCode;
}
