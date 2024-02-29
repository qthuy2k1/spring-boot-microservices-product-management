package com.qthuy2k1.productservice.controller;

import com.qthuy2k1.productservice.dto.ProductRequest;
import com.qthuy2k1.productservice.dto.ProductResponse;
import com.qthuy2k1.productservice.exception.NotFoundException;
import com.qthuy2k1.productservice.service.ProductService;
import jakarta.validation.Valid;
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
    public ResponseEntity<String> createProduct(@RequestBody @Valid ProductRequest productRequest) throws NotFoundException {
        productService.createProduct(productRequest);
        return new ResponseEntity<>("Success", HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();

        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @PutMapping("{id}")
    public ResponseEntity<String> updateProduct(@PathVariable("id") String id, @RequestBody @Valid ProductRequest productRequest)
            throws NotFoundException, NumberFormatException {
        Integer parsedId = Integer.valueOf(id);
        productService.updateProductById(parsedId, productRequest);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable("id") String id)
            throws NotFoundException, NumberFormatException {
        Integer parsedId = Integer.valueOf(id);
        productService.deleteProductById(parsedId);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable("id") String id)
            throws NumberFormatException, NotFoundException {
        Integer parsedId = Integer.valueOf(id);
        ProductResponse product = productService.getProductById(parsedId);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @GetMapping("{id}/is-exists")
    public Boolean isProductExists(@PathVariable("id") String id) throws NumberFormatException {
        Integer parsedId = Integer.valueOf(id);
        return productService.isProductExists(parsedId);
    }
}
