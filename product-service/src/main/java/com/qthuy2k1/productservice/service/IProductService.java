package com.qthuy2k1.productservice.service;

import com.qthuy2k1.productservice.dto.request.ProductRequest;
import com.qthuy2k1.productservice.dto.response.ProductGraphQLResponse;
import com.qthuy2k1.productservice.dto.response.ProductResponse;

import java.util.List;
import java.util.Set;

public interface IProductService {
    ProductResponse createProduct(ProductRequest productRequest);

    List<ProductResponse> getAllProducts();

    List<ProductGraphQLResponse> getAllProductsGraphQL();

    ProductResponse updateProductById(int id, ProductRequest productRequest);

    void deleteProductById(int id);

    ProductResponse getProductById(int id);

    Boolean isProductExists(int id);

    ProductGraphQLResponse getProductGraphQLById(int id);

    List<ProductResponse> getProductByListId(Set<Integer> ids);

    List<ProductGraphQLResponse> getProductGraphQLByListId(Set<Integer> ids);

    void batchInsertProduct(List<ProductRequest> productList);
}
