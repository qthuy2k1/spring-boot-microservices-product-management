package com.qthuy2k1.productservice.controller;

import com.qthuy2k1.productservice.dto.request.ProductRequest;
import com.qthuy2k1.productservice.dto.response.ProductGraphQLResponse;
import com.qthuy2k1.productservice.enums.ErrorCode;
import com.qthuy2k1.productservice.exception.AppException;
import com.qthuy2k1.productservice.service.IProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GraphQLController {
    IProductService productService;

    @QueryMapping
    public ProductGraphQLResponse productById(@Argument("id") int id) {
        return productService.getProductGraphQLById(id);
    }

    @MutationMapping
    public String createProduct(@Argument("input") ProductRequest productRequest) {
        productService.createProduct(productRequest);
        return "Success";
    }

    @QueryMapping
    public List<ProductGraphQLResponse> getProducts() {
        return productService.getAllProductsGraphQL();
    }

    @QueryMapping
    public List<ProductGraphQLResponse> getProductGraphQLByListId(@Argument("ids") List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new AppException(ErrorCode.INVALID_REQUEST_VARIABLE);
        }
        return productService.getProductGraphQLByListId(new HashSet<>(ids));
    }
}
