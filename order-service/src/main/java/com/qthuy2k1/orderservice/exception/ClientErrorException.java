package com.qthuy2k1.orderservice.exception;

import lombok.Getter;

@Getter
public class ClientErrorException extends RuntimeException {
    private final int statusCode;

    public ClientErrorException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
