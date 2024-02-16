package com.qthuy2k1.product.controller;

import com.qthuy2k1.product.dto.ProductRequest;
import com.qthuy2k1.product.dto.ProductResponse;
import com.qthuy2k1.product.exception.ProductCategoryNotFoundException;
import com.qthuy2k1.product.exception.UserNotFoundException;
import com.qthuy2k1.product.service.ProductService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "user", fallbackMethod = "fallbackMethodCreateProduct")
//    @TimeLimiter(name = "user")
    public ResponseEntity<String> createProduct(@RequestBody ProductRequest productRequest) throws ProductCategoryNotFoundException, UserNotFoundException {
        productService.createProduct(productRequest);
        return new ResponseEntity<>("Success", HttpStatus.CREATED);
    }

    public ResponseEntity<String> fallbackMethodCreateProduct(ProductRequest productRequest, RuntimeException runtimeException) {
        return new ResponseEntity<>("Oops! Something went wrong!", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }
}
