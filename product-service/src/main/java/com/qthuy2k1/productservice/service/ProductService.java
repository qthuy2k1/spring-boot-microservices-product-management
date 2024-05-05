package com.qthuy2k1.productservice.service;

import com.qthuy2k1.productservice.dto.*;
import com.qthuy2k1.productservice.exception.NotFoundEnumException;
import com.qthuy2k1.productservice.exception.NotFoundException;
import com.qthuy2k1.productservice.feign.IInventoryClient;
import com.qthuy2k1.productservice.model.ProductCategoryModel;
import com.qthuy2k1.productservice.model.ProductModel;
import com.qthuy2k1.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
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
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductCategoryService productCategoryService;
    private final IInventoryClient inventoryClient;

    public void createProduct(ProductRequest productRequest) throws NotFoundException {
        ProductModel productModel = convertToProductModel(productRequest);

        // Get the product category
        ProductCategoryResponse productCategory = productCategoryService.getProductCategoryById(productRequest.getCategoryId());
        productModel.setCategory(convertToProductCategoryModel(productCategory));

        ProductModel productSaved = productRepository.save(productModel);

        // create inventory with quantity and product id
        InventoryRequest inventoryRequest = InventoryRequest.builder()
                .quantity(productRequest.getQuantity())
                .productId(productSaved.getId())
                .build();

        inventoryClient.createInventory(inventoryRequest);
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

    public void batchInsertProduct(List<ProductRequest> productList) {
        List<ProductModel> productModelList = new ArrayList<>();
        for (ProductRequest product : productList) {
            // create inventory with quantity and product id
            ProductCategoryResponse productCategory = productCategoryService.getProductCategoryById(product.getCategoryId());
            ProductModel productModel = convertToProductModel(product);
            productModel.setCategory(convertToProductCategoryModel(productCategory));
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

    private ProductResponse convertToProductResponse(ProductModel productModel, ProductCategoryResponse productCategoryResponse) {
        return ProductResponse.builder()
                .id(productModel.getId())
                .name(productModel.getName())
                .description(productModel.getDescription())
                .price(productModel.getPrice())
                .skuCode(productModel.getSkuCode())
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
