package com.qthuy2k1.productservice.controller;

import com.qthuy2k1.productservice.dto.request.ProductRequest;
import com.qthuy2k1.productservice.dto.response.ProductGraphQLResponse;
import com.qthuy2k1.productservice.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GraphQLController {
    ProductService productService;

    @QueryMapping
    public ProductGraphQLResponse productById(@Argument("id") int id) {
        var product = productService.getProductGraphQLById(id);
        return product;
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
}
