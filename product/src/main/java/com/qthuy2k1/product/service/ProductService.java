package com.qthuy2k1.product.service;

import com.qthuy2k1.product.dto.ProductCategoryResponse;
import com.qthuy2k1.product.dto.ProductRequest;
import com.qthuy2k1.product.dto.ProductResponse;
import com.qthuy2k1.product.exception.NotFoundEnumException;
import com.qthuy2k1.product.exception.NotFoundException;
import com.qthuy2k1.product.model.ProductCategoryModel;
import com.qthuy2k1.product.model.ProductModel;
import com.qthuy2k1.product.repository.ProductRepository;
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

        // Check user exists
        Boolean isUserExists = webClientBuilder.build().get()
                .uri("http://user/api/v1/users/" + productModel.getUserId() + "/is-exists")
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        if (isUserExists != null && isUserExists.equals(false)) {
            throw new NotFoundException(NotFoundEnumException.USER);
        }

        // Get the product category
        ProductCategoryResponse productCategory = productCategoryService.getProductCategoryById(productRequest.getCategoryId());
        productModel.setCategory(convertToProductCategoryModel(productCategory));

        productRepository.save(productModel);
    }

    public List<ProductResponse> getAllProducts() {
        List<ProductModel> products = productRepository.findAll();
        return products.stream().map(this::convertToProductResponse).toList();
    }

    public void updateProductById(Integer id, ProductRequest productRequest) throws NotFoundException {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NotFoundEnumException.PRODUCT));

        // Check user exists
        Boolean isUserExists = webClientBuilder.build().get()
                .uri("http://user/api/v1/users/" + productRequest.getUserId() + "/is-exists")
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        if (isUserExists != null && isUserExists.equals(false)) {
            throw new NotFoundException(NotFoundEnumException.USER);
        }

        // Get the product category
        ProductCategoryResponse productCategory = productCategoryService.getProductCategoryById(productRequest.getCategoryId());

        // Update the product
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setSkuCode(productRequest.getSkuCode());
        product.setUserId(productRequest.getUserId());
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


    private ProductModel convertToProductModel(ProductRequest productRequest) {
        return ProductModel.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .userId(productRequest.getUserId())
                .skuCode(productRequest.getSkuCode())
                .build();
    }

    private ProductResponse convertToProductResponse(ProductModel productModel) {
        return ProductResponse.builder()
                .id(productModel.getId())
                .name(productModel.getName())
                .description(productModel.getDescription())
                .price(productModel.getPrice())
                .userId(productModel.getUserId())
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
