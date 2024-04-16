package com.qthuy2k1.productservice.controller;

import com.qthuy2k1.productservice.dto.ProductGraphQLResponse;
import com.qthuy2k1.productservice.dto.ProductRequest;
import com.qthuy2k1.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GraphQLController {
    private final ProductService productService;

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
}
