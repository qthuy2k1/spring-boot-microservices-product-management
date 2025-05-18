package com.qthuy2k1.productservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qthuy2k1.productservice.dto.request.ProductRequest;
import com.qthuy2k1.productservice.dto.response.PaginatedResponse;
import com.qthuy2k1.productservice.dto.response.ProductGraphQLResponse;
import com.qthuy2k1.productservice.dto.response.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface IProductService {
    ProductResponse createProduct(ProductRequest productRequest) throws JsonProcessingException;

    PaginatedResponse<ProductResponse> getAllProducts(int page, int size);

    List<ProductGraphQLResponse> getAllProductsGraphQL();

    ProductResponse updateProductById(int id, ProductRequest productRequest);

    void deleteProductById(int id);

    ProductResponse getProductById(int id);

    Boolean isProductExists(int id);

    ProductGraphQLResponse getProductGraphQLById(int id);

    List<ProductResponse> getProductByListId(String ids);

    List<ProductGraphQLResponse> getProductGraphQLByListId(Set<Integer> ids);

    void batchInsertProduct(MultipartFile file) throws IOException;
}
