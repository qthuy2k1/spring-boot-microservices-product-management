package com.qthuy2k1.product.exception;

public enum NotFoundEnumException {
    USER("User not found"),
    PRODUCT("Product not found"),
    PRODUCT_CATEGORY("Product category not found");

    private final String message;

    NotFoundEnumException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static NotFoundEnumException create(String resourceType) {
        return switch (resourceType) {
            case "user" -> USER;
            case "product" -> PRODUCT;
            case "productCategory" -> PRODUCT_CATEGORY;
            default -> throw new IllegalArgumentException("Invalid resource type: " + resourceType);
        };
    }
}
