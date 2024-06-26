package com.qthuy2k1.orderservice.advice;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.orderservice.exception.ClientErrorException;
import com.qthuy2k1.orderservice.exception.NotFoundException;
import com.qthuy2k1.orderservice.exception.ProductOutOfStock;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.io.IOException;
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NumberFormatException.class)
    public Map<String, String> handleInvalidIdException(NumberFormatException ex) {
        log.error(ex.getMessage() + " - " + ex.getCause());
        return Map.of("error", "invalid id");
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public Map<String, String> handleResourceNotFoundException(NotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(JsonMappingException.class)
    public Map<String, String> handleJsonMappingException(JsonMappingException ex) {
        log.error("Error json mapping exception: " + ex.getCause() + " - " + ex.getMessage());
        return Map.of("error", "json bad format");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(WebClientRequestException.class)
    public Map<String, String> handleRequestToOtherResourceException(WebClientRequestException ex) {
        log.error("Error request to other resource exception: " + ex.getCause() + " - " + ex.getMessage());
        return Map.of("error", "internal server error");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ProductOutOfStock.class)
    public Map<String, String> handleProductOutOfStockException(ProductOutOfStock ex) {
        return Map.of("error", "product is out of stock, please try again later");
    }

    @ExceptionHandler(ClientErrorException.class)
    public Map<String, String> handleFeignStatusException(ClientErrorException e, HttpServletResponse response) {
        response.setStatus(e.getStatusCode());
        String message;
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> responseMap = mapper.readValue(e.getMessage(), Map.class);
            message = responseMap.get("error");
        } catch (IOException ex) {
            message = "unknown error";
            System.err.println("Error parsing JSON response: " + e.getMessage());
        }
        return Map.of("error", message);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Map<String, String> handleUnwantedException(Exception ex) {
        log.error("Error exception class: " + ex.getCause() + " - " + ex.getMessage());

        // Return "unknown error" to the users instead of the actual errors message
        return Map.of("error", "unknown error");
    }

}
