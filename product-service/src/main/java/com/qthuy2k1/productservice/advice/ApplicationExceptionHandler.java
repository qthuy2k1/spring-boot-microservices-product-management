package com.qthuy2k1.productservice.advice;

import com.qthuy2k1.productservice.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ApplicationExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleInvalidArgument(MethodArgumentNotValidException ex) {
        Map<String, String> errorMap = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorMap.put(error.getField(), error.getDefaultMessage());
        });

        return errorMap;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public Map<String, String> handleResourceNotFoundException(NotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NumberFormatException.class)
    public Map<String, String> handleInvalidIdException(NumberFormatException ex) {
        log.error(ex.getMessage());
        return Map.of("error", "invalid id");
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AccessDeniedException.class)
    public Map<String, String> handleUnauthorizedException(AccessDeniedException ex) {
        log.error("ERROR AccessDeniedException: " + ex.getMessage());
        return Map.of("error", "access denied");
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public Map<String, String> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.error("ERROR NoResourceFoundException: " + ex.getMessage());
        return Map.of("error", "resource not found");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Map<String, String> handleUnwantedException(Exception ex) {
        log.error("ERROR exception class: " + ex.getClass() + " - " + ex.getCause() + " - " + ex.getMessage());

        // Return "unknown error" to the users instead of the actual errors message
        return Map.of("error", "unknown error");
    }
}
