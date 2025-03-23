package com.qthuy2k1.productservice.service;

import com.qthuy2k1.productservice.dto.request.ProductCategoryRequest;
import com.qthuy2k1.productservice.dto.response.ProductCategoryResponse;
import com.qthuy2k1.productservice.enums.ErrorCode;
import com.qthuy2k1.productservice.exception.AppException;
import com.qthuy2k1.productservice.mapper.ProductCategoryMapper;
import com.qthuy2k1.productservice.model.ProductCategoryModel;
import com.qthuy2k1.productservice.repository.ProductCategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductCategoryService implements IProductCategoryService {
    ProductCategoryRepository productCategoryRepository;
    ProductCategoryMapper productCategoryMapper;

    public ProductCategoryResponse createProductCategory(ProductCategoryRequest productCategoryRequest) {
        ProductCategoryModel productCategoryModel = productCategoryMapper.toProductCategory(productCategoryRequest);
        return productCategoryMapper.toProductCategoryResponse(productCategoryRepository.save(productCategoryModel));
    }

    public List<ProductCategoryResponse> getAllProductCategories() {
        List<ProductCategoryModel> productCategories = productCategoryRepository.findAll();
        return productCategories.stream().map(productCategoryMapper::toProductCategoryResponse).toList();
    }

    public ProductCategoryResponse getProductCategoryById(Integer id) {
        ProductCategoryModel productCategory = productCategoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
        return productCategoryMapper.toProductCategoryResponse(productCategory);
    }
}
