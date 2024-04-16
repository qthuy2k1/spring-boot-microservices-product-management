package com.qthuy2k1.productservice.service;

import com.qthuy2k1.productservice.dto.*;
import com.qthuy2k1.productservice.exception.NotFoundEnumException;
import com.qthuy2k1.productservice.exception.NotFoundException;
import com.qthuy2k1.productservice.model.ProductCategoryModel;
import com.qthuy2k1.productservice.model.ProductModel;
import com.qthuy2k1.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductCategoryService productCategoryService;
    @LoadBalanced
    private final WebClient.Builder webClientBuilder;

    public void createProduct(ProductRequest productRequest) throws NotFoundException {
        ProductModel productModel = convertToProductModel(productRequest);

        // Get the product category
        ProductCategoryResponse productCategory = productCategoryService.getProductCategoryById(productRequest.getCategoryId());
        productModel.setCategory(convertToProductCategoryModel(productCategory));

        productRepository.save(productModel);

        InventoryRequest inventoryRequest = InventoryRequest.builder()
                .quantity(productRequest.getQuantity())
                .skuCode(productRequest.getSkuCode())
                .build();

        webClientBuilder.build()
                .post()
                .uri("http://inventory-service/api/v1/inventories")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryRequest)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }

    public List<ProductResponse> getAllProducts() {
        List<ProductModel> products = productRepository.findAll();
        return products
                .stream()
                .map(product -> convertToProductResponse(product,
                        convertToProductCategoryResponse(product.getCategory()))).toList();
    }

    public List<ProductGraphQLResponse> getAllProductsGraphQL() {
        List<ProductModel> products = productRepository.findAll();
        return products.stream().map(product -> convertToProductGraphQLResponse(product, convertToProductCategoryResponse(product.getCategory()))).toList();
    }

    @CachePut(cacheNames = "products", key = "#p0", condition = "#p0!=null")
    public void updateProductById(Integer id, ProductRequest productRequest) throws NotFoundException {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NotFoundEnumException.PRODUCT));

        // Get the product category
        ProductCategoryResponse productCategory = productCategoryService.getProductCategoryById(productRequest.getCategoryId());

        // Update the product
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setCategory(convertToProductCategoryModel(productCategory));

        productRepository.save(product);
    }

    @CacheEvict(cacheNames = "products", key = "#p0", condition = "#p0!=null")
    public void deleteProductById(Integer id) throws NotFoundException {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NotFoundEnumException.PRODUCT));

        productRepository.delete(product);
    }

    @Cacheable(cacheNames = "products", key = "#p0", condition = "#p0!=null")
    public ProductResponse getProductById(Integer id) throws NotFoundException {
        log.info("fetching from db");
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NotFoundEnumException.PRODUCT));

        return convertToProductResponse(product, convertToProductCategoryResponse(product.getCategory()));
    }

    @Cacheable(cacheNames = "products", key = "#p0", condition = "#p0!=null")
    public Boolean isProductExists(Integer id) {
        log.info("fetching from db");
        return productRepository.existsById(id);
    }


    public ProductGraphQLResponse getProductGraphQLById(Integer id) throws NotFoundException {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NotFoundEnumException.PRODUCT));

        return convertToProductGraphQLResponse(product, convertToProductCategoryResponse(product.getCategory()));
    }

    private ProductGraphQLResponse convertToProductGraphQLResponse(ProductModel productModel, ProductCategoryResponse productCategoryResponse) {
        return ProductGraphQLResponse.builder()
                .id(productModel.getId())
                .name(productModel.getName())
                .description(productModel.getDescription())
                .price(productModel.getPrice())
                .category(productCategoryResponse)
                .build();
    }

    private ProductCategoryResponse convertToProductCategoryResponse(ProductCategoryModel productCategoryModel) {
        return ProductCategoryResponse.builder()
                .id(productCategoryModel.getId())
                .name(productCategoryModel.getName())
                .description(productCategoryModel.getDescription())
                .build();
    }

    private ProductModel convertToProductModel(ProductRequest productRequest) {
        return ProductModel.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();
    }

    private ProductResponse convertToProductResponse(ProductModel productModel, ProductCategoryResponse productCategoryResponse) {
        return ProductResponse.builder()
                .id(productModel.getId())
                .name(productModel.getName())
                .description(productModel.getDescription())
                .price(productModel.getPrice())
                .category(productCategoryResponse)
                .build();
    }


    private ProductCategoryModel convertToProductCategoryModel(ProductCategoryResponse productCategoryResponse) {
        return ProductCategoryModel.builder()
                .id(productCategoryResponse.getId())
                .name(productCategoryResponse.getName())
                .description(productCategoryResponse.getDescription())
                .build();
    }
}
