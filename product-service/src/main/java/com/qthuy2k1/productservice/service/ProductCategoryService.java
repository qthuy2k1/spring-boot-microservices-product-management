package com.qthuy2k1.productservice.service;

import com.qthuy2k1.productservice.dto.ProductCategoryRequest;
import com.qthuy2k1.productservice.dto.ProductCategoryResponse;
import com.qthuy2k1.productservice.exception.NotFoundEnumException;
import com.qthuy2k1.productservice.exception.NotFoundException;
import com.qthuy2k1.productservice.model.ProductCategoryModel;
import com.qthuy2k1.productservice.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {
    private final ProductCategoryRepository productCategoryRepository;

    public void createProductCategory(ProductCategoryRequest productCategoryRequest) {
        ProductCategoryModel productCategoryModel = convertToProductCategoryModel(productCategoryRequest);

        productCategoryRepository.save(productCategoryModel);
    }

    public List<ProductCategoryResponse> getAllProductCategories() {
        List<ProductCategoryModel> productCategories = productCategoryRepository.findAll();
        return productCategories.stream().map(this::convertToProductCategoryResponse).toList();
    }

    public ProductCategoryResponse getProductCategoryById(Integer id) throws NotFoundException {
        ProductCategoryModel productCategory = productCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NotFoundEnumException.PRODUCT_CATEGORY));

        return convertToProductCategoryResponse(productCategory);
    }

    private ProductCategoryModel convertToProductCategoryModel(ProductCategoryRequest productCategoryRequest) {
        return ProductCategoryModel.builder()
                .name(productCategoryRequest.getName())
                .description(productCategoryRequest.getDescription())
                .build();
    }

    private ProductCategoryResponse convertToProductCategoryResponse(ProductCategoryModel productCategoryModel) {
        return ProductCategoryResponse.builder()
                .id(productCategoryModel.getId())
                .name(productCategoryModel.getName())
                .description(productCategoryModel.getDescription())
                .build();
    }
}
