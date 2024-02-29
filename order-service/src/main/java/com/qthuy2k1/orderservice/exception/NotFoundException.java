package com.qthuy2k1.orderservice.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    private final NotFoundEnumException exceptionType;

    public NotFoundException(NotFoundEnumException exceptionType) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
    }

}
