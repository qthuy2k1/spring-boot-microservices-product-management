package com.qthuy2k1.productservice.controller;

import com.qthuy2k1.productservice.dto.ProductCategoryRequest;
import com.qthuy2k1.productservice.dto.ProductCategoryResponse;
import com.qthuy2k1.productservice.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product-categories")
public class ProductCategoryController {
    private final ProductCategoryService productCategoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createProductCategory(@RequestBody ProductCategoryRequest productCategoryRequest) {
        productCategoryService.createProductCategory(productCategoryRequest);
        return "Success";
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductCategoryResponse> getAllProductCategories() {
        return productCategoryService.getAllProductCategories();
    }
}
