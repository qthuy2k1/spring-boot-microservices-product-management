package com.qthuy2k1.productservice.service;

import com.qthuy2k1.productservice.dto.ProductCategoryResponse;
import com.qthuy2k1.productservice.dto.ProductGraphQLResponse;
import com.qthuy2k1.productservice.dto.ProductRequest;
import com.qthuy2k1.productservice.dto.ProductResponse;
import com.qthuy2k1.productservice.exception.NotFoundEnumException;
import com.qthuy2k1.productservice.exception.NotFoundException;
import com.qthuy2k1.productservice.model.ProductCategoryModel;
import com.qthuy2k1.productservice.model.ProductModel;
import com.qthuy2k1.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
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
    }

    public List<ProductResponse> getAllProducts() {
        List<ProductModel> products = productRepository.findAll();
        return products.stream().map(this::convertToProductResponse).toList();
    }

    public List<ProductGraphQLResponse> getAllProductsGraphQL() {
        List<ProductModel> products = productRepository.findAll();
        return products.stream().map(product -> convertToProductGraphQLResponse(product, convertToProductCategoryResponse(product.getCategory()))).toList();
    }

    public void updateProductById(Integer id, ProductRequest productRequest) throws NotFoundException {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NotFoundEnumException.PRODUCT));

        // Get the product category
        ProductCategoryResponse productCategory = productCategoryService.getProductCategoryById(productRequest.getCategoryId());

        // Update the product
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setSkuCode(productRequest.getSkuCode());
        product.setCategory(convertToProductCategoryModel(productCategory));

        productRepository.save(product);
    }

    public void deleteProductById(Integer id) throws NotFoundException {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NotFoundEnumException.PRODUCT));

        productRepository.delete(product);
    }

    public ProductResponse getProductById(Integer id) throws NotFoundException {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NotFoundEnumException.PRODUCT));

        return convertToProductResponse(product);
    }

    public Boolean isProductExists(Integer id) {
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
                .skuCode(productModel.getSkuCode())
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
                .skuCode(productRequest.getSkuCode())
                .build();
    }

    private ProductResponse convertToProductResponse(ProductModel productModel) {
        return ProductResponse.builder()
                .id(productModel.getId())
                .name(productModel.getName())
                .description(productModel.getDescription())
                .price(productModel.getPrice())
                .categoryId(productModel.getCategory().getId())
                .skuCode(productModel.getSkuCode())
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
