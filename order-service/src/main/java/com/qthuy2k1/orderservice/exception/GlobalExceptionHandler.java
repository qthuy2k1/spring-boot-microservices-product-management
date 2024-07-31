package com.qthuy2k1.orderservice.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.orderservice.dto.response.ApiResponse;
import com.qthuy2k1.orderservice.enums.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidArgument(MethodArgumentNotValidException ex) {
        String enumKey = Objects.requireNonNull(ex.getFieldError()).getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        // if validation exception is configured wrong
        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        }

        return ResponseEntity.badRequest().body(
                ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        return ResponseEntity.status(errorCode.getStatusCode()).body(
                ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity.status(errorCode.getStatusCode()).body(
                ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnwantedException(Exception ex) {
        log.error("Error exception class: {} - {} - {}", ex.getClass(), ex.getCause(), ex.getMessage());

        // Return unknown error to the users instead of the actual errors message
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        return ResponseEntity.status(errorCode.getStatusCode()).body(
                ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(ClientErrorException.class)
    public ResponseEntity<ApiResponse<Void>> handleFeignStatusException(ClientErrorException e, HttpServletResponse response) {
        response.setStatus(e.getStatusCode());
        String message;
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> responseMap = mapper.readValue(e.getMessage(), Map.class);
            message = responseMap.get("error");
        } catch (IOException ex) {
            message = "unknown error";
            log.error("Error parsing JSON response: {}", e.getMessage());
        }
        return ResponseEntity.status(e.getStatusCode()).body(ApiResponse.<Void>builder()
                .code(ErrorCode.FEIGN_ERROR.getCode())
                .message(message)
                .build());
    }
}
