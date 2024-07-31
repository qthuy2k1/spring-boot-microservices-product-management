package com.qthuy2k1.productservice.mapper;

import com.qthuy2k1.productservice.dto.request.ProductCategoryRequest;
import com.qthuy2k1.productservice.dto.response.ProductCategoryResponse;
import com.qthuy2k1.productservice.model.ProductCategoryModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductCategoryMapper {
    ProductCategoryModel toProductCategory(ProductCategoryRequest request);

    ProductCategoryResponse toProductCategoryResponse(ProductCategoryModel productCategoryModel);
}
