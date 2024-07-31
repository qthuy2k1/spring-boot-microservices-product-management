package com.qthuy2k1.productservice.mapper;

import com.qthuy2k1.productservice.dto.request.ProductRequest;
import com.qthuy2k1.productservice.dto.response.ProductGraphQLResponse;
import com.qthuy2k1.productservice.dto.response.ProductResponse;
import com.qthuy2k1.productservice.model.ProductModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductModel toProduct(ProductRequest request);

    //    @Mapping(target = "category", ignore = true)
    ProductResponse toProductResponse(ProductModel productModel);

    //    @Mapping(target = "category", ignore = true)
    ProductGraphQLResponse toProductGraphQLResponse(ProductModel productModel);
}
