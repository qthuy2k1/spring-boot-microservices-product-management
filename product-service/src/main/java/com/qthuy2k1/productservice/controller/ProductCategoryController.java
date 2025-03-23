package com.qthuy2k1.productservice.controller;

import com.qthuy2k1.productservice.dto.request.ProductCategoryRequest;
import com.qthuy2k1.productservice.dto.response.ApiResponse;
import com.qthuy2k1.productservice.dto.response.ProductCategoryResponse;
import com.qthuy2k1.productservice.service.IProductCategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product-categories")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductCategoryController {
    IProductCategoryService productCategoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> createProductCategory(@RequestBody ProductCategoryRequest productCategoryRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ProductCategoryResponse>builder()
                        .result(productCategoryService.createProductCategory(productCategoryRequest))
                        .build()
        );
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<List<ProductCategoryResponse>>> getAllProductCategories() {
        return ResponseEntity.ok().body(
                ApiResponse.<List<ProductCategoryResponse>>builder()
                        .result(productCategoryService.getAllProductCategories())
                        .build()
        );
    }
}
