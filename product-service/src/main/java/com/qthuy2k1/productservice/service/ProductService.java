package com.qthuy2k1.productservice.service;

import com.qthuy2k1.productservice.dto.request.InventoryRequest;
import com.qthuy2k1.productservice.dto.request.ProductRequest;
import com.qthuy2k1.productservice.dto.response.ProductGraphQLResponse;
import com.qthuy2k1.productservice.dto.response.ProductResponse;
import com.qthuy2k1.productservice.enums.ErrorCode;
import com.qthuy2k1.productservice.exception.AppException;
import com.qthuy2k1.productservice.mapper.ProductMapper;
import com.qthuy2k1.productservice.model.ProductCategoryModel;
import com.qthuy2k1.productservice.model.ProductModel;
import com.qthuy2k1.productservice.repository.ProductCategoryRepository;
import com.qthuy2k1.productservice.repository.ProductRepository;
import com.qthuy2k1.productservice.repository.feign.InventoryClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {
    ProductRepository productRepository;
    ProductCategoryRepository productCategoryRepository;
    InventoryClient inventoryClient;
    ProductMapper productMapper;

    public ProductResponse createProduct(ProductRequest productRequest) {
        ProductModel productModel = productMapper.toProduct(productRequest);

        // Get the product category
        ProductCategoryModel productCategory =
                productCategoryRepository.findById(productRequest.getCategoryId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
        productModel.setCategory(productCategory);

        productModel = productRepository.save(productModel);

        // Create inventory with quantity and product id
        InventoryRequest inventoryRequest = InventoryRequest.builder()
                .quantity(productRequest.getQuantity())
                .productId(productModel.getId())
                .build();

        inventoryClient.createInventory(inventoryRequest);

        return productMapper.toProductResponse(productModel);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(productMapper::toProductResponse).toList();
    }

    public List<ProductGraphQLResponse> getAllProductsGraphQL() {
        return productRepository.findAll().stream().map(productMapper::toProductGraphQLResponse).toList();
    }

    @CachePut(cacheNames = "products", key = "#p0", condition = "#p0!=null", unless = "#result==null")
    public ProductResponse updateProductById(int id, ProductRequest productRequest) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // Get the product category
        ProductCategoryModel productCategory =
                productCategoryRepository.findById(productRequest.getCategoryId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));

        // Update the product
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setCategory(productCategory);

        return productMapper.toProductResponse(productRepository.save(product));
    }

    @CacheEvict(cacheNames = "products", key = "#p0", condition = "#p0!=null")
    public void deleteProductById(int id) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        productRepository.delete(product);
    }

    @Cacheable(cacheNames = "products", key = "#p0", condition = "#p0!=null")
    public ProductResponse getProductById(int id) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        return productMapper.toProductResponse(product);
    }

    @Cacheable(cacheNames = "products", key = "#p0", condition = "#p0!=null")
    public Boolean isProductExists(int id) {
        return productRepository.existsById(id);
    }


    public ProductGraphQLResponse getProductGraphQLById(int id) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));


        return productMapper.toProductGraphQLResponse(product);
    }

    public void batchInsertProduct(List<ProductRequest> productList) {
        List<ProductModel> productModelList = new ArrayList<>();
        for (ProductRequest product : productList) {
            // create inventory with quantity and product id
            ProductCategoryModel productCategory =
                    productCategoryRepository.findById(product.getCategoryId())
                            .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
            ProductModel productModel = productMapper.toProduct(product);
            productModel.setCategory(productCategory);
            productModelList.add(productModel);
        }

        List<ProductModel> productSavedList = productRepository.saveAll(productModelList);
        if (productSavedList.size() == productList.size()) {
            for (int i = 0; i < productSavedList.size(); i++) {
                // create inventory with quantity and product id
                InventoryRequest inventoryRequest = InventoryRequest.builder()
                        .quantity(productList.get(i).getQuantity())
                        .productId(productSavedList.get(i).getId())
                        .build();
                inventoryClient.createInventory(inventoryRequest);
            }
        } else {
            throw new RuntimeException("the size between request and response product list isn't the same");
        }
    }
}
