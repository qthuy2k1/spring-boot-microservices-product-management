package com.qthuy2k1.orderservice.exception;

import lombok.Getter;

@Getter
public enum NotFoundEnumException {
    USER("User not found"),
    PRODUCT("Product not found"),
    ORDER("Order not found");

    private final String message;

    NotFoundEnumException(String message) {
        this.message = message;
    }

    public static NotFoundEnumException create(String resourceType) {
        return switch (resourceType) {
            case "user" -> USER;
            case "product" -> PRODUCT;
            case "order" -> ORDER;
            default -> throw new IllegalArgumentException("Invalid resource type: " + resourceType);
        };
    }
}
