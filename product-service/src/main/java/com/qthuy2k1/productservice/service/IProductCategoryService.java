package com.qthuy2k1.productservice.service;

import com.qthuy2k1.productservice.dto.request.ProductCategoryRequest;
import com.qthuy2k1.productservice.dto.response.ProductCategoryResponse;

import java.util.List;

public interface IProductCategoryService {
    ProductCategoryResponse createProductCategory(ProductCategoryRequest productCategoryRequest);

    List<ProductCategoryResponse> getAllProductCategories();

    ProductCategoryResponse getProductCategoryById(Integer id);
}
