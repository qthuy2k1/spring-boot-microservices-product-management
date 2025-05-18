package com.qthuy2k1.productservice.unit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qthuy2k1.productservice.dto.request.InventoryRequest;
import com.qthuy2k1.productservice.dto.request.ProductRequest;
import com.qthuy2k1.productservice.dto.response.ProductResponse;
import com.qthuy2k1.productservice.enums.ErrorCode;
import com.qthuy2k1.productservice.mapper.ProductMapper;
import com.qthuy2k1.productservice.model.ProductCategoryModel;
import com.qthuy2k1.productservice.model.ProductModel;
import com.qthuy2k1.productservice.repository.ProductCategoryRepository;
import com.qthuy2k1.productservice.repository.ProductRepository;
import com.qthuy2k1.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Spy
    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);
    int defaultPage = 0;
    int defaultSize = 10;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductCategoryRepository productCategoryRepository;
    @InjectMocks
    private ProductService productService;
    @Mock
    private KafkaTemplate<String, InventoryRequest> inventoryRequestKafkaTemplate;
    @Mock
    private KafkaTemplate<String, List<InventoryRequest>> inventoryRequestListKafkaTemplate;

    @Test
    void createProduct() throws JsonProcessingException {
        // Given
        ProductRequest productRequest = ProductRequest.builder()
                .name("Product 1")
                .description("Product description 1")
                .price(BigDecimal.valueOf(1))
                .categoryId(1)
                .skuCode("abc")
                .quantity(1)
                .build();
        ProductCategoryModel productCategory = new ProductCategoryModel(1, "abc", "abc", Set.of());

        ProductModel productModel = productMapper.toProduct(productRequest);
        productModel.setCategory(productCategory);

        given(productCategoryRepository.findById(productRequest.getCategoryId())).willReturn(Optional.of(productCategory));


        // When
        when(productRepository.save(any())).thenReturn(productModel);
        productService.createProduct(productRequest);

        // Then
        ArgumentCaptor<ProductModel> productArgumentCaptor = ArgumentCaptor.forClass(ProductModel.class);
        then(productRepository).should().save(productArgumentCaptor.capture());

        ProductModel capturedProduct = productArgumentCaptor.getValue();

        assertThat(capturedProduct.getName()).isEqualTo(productModel.getName());
        assertThat(capturedProduct.getDescription()).isEqualTo(productModel.getDescription());
        assertThat(capturedProduct.getPrice()).isEqualTo(productModel.getPrice());
        assertThat(capturedProduct.getCategory().getId()).isEqualTo(productModel.getCategory().getId());
    }

    @Test
    void getAllProducts() {
        // Given
        List<ProductModel> products = new ArrayList<>();
        ProductCategoryModel productCategoryModel = ProductCategoryModel.builder()
                .name("category 1")
                .description("description of category 1")
                .build();

        products.add(
                ProductModel.builder()
                        .name("iphone 13")
                        .description("description of iphone 13")
                        .price(BigDecimal.valueOf(1000))
                        .category(productCategoryModel)
                        .build()
        );
        products.add(
                ProductModel.builder()
                        .name("iphone 12")
                        .description("description of iphone 12")
                        .price(BigDecimal.valueOf(900))
                        .category(productCategoryModel)
                        .build()
        );
        Page<ProductModel> productPage = new PageImpl<>(products);
        given(productRepository.findAll(any(PageRequest.class))).willReturn(productPage);

        // When
        productService.getAllProducts(defaultPage, defaultSize);

        // Then
        then(productRepository).should().findAll(any(PageRequest.class));
    }

    @Test
    void deleteProductById() {
        // given
        ProductCategoryModel productCategory = ProductCategoryModel.builder()
                .id(1)
                .name("category 1")
                .description("description 1")
                .build();
        ProductModel product = ProductModel.builder()
                .id(1)
                .name("product 1")
                .description("des 1")
                .price(BigDecimal.valueOf(1))
                .category(productCategory)
                .build();
        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));

        // When
        productService.deleteProductById(product.getId());

        // Then
        then(productRepository).should().delete(product);
    }

    @Test
    void deleteProductById_ExceptionThrown_ProductNotFound() {
        // given
        int id = 1;
        given(productRepository.findById(id)).willReturn(Optional.empty());

        // When
        assertThatThrownBy(() ->
                productService.deleteProductById(id))
                .hasMessageContaining(ErrorCode.PRODUCT_NOT_FOUND.getMessage());

        // Then
        then(productRepository).should(never()).delete(any());
    }

    @Test
    void getProductById() {
        // given
        int id = 1;
        ProductCategoryModel productCategory = ProductCategoryModel.builder()
                .id(1)
                .name("category 1")
                .description("description 1")
                .build();
        ProductModel productModel = ProductModel.builder()
                .id(1)
                .name("product 1")
                .description("des 1")
                .price(BigDecimal.valueOf(1))
                .category(productCategory)
                .skuCode("ABC")
                .build();
        ProductResponse productModelResponse = productMapper.toProductResponse(productModel);
        given(productRepository.findById(id)).willReturn(Optional.of(productModel));

        // When
        ProductResponse product = productService.getProductById(id);

        // Then
        assertThat(product.getId()).isEqualTo(productModelResponse.getId());
        assertThat(product.getName()).isEqualTo(productModelResponse.getName());
        assertThat(product.getDescription()).isEqualTo(productModelResponse.getDescription());
        assertThat(product.getPrice()).isEqualTo(productModelResponse.getPrice());
        assertThat(product.getSkuCode()).isEqualTo(productModelResponse.getSkuCode());

        then(productRepository).should().findById(id);
    }

    @Test
    void getProductById_ExceptionThrown_ProductNotFound() {
        // given
        int id = 1;
        given(productRepository.findById(id)).willReturn(Optional.empty());

        // When
        assertThatThrownBy(() ->
                productService.getProductById(id))
                .hasMessageContaining(ErrorCode.PRODUCT_NOT_FOUND.getMessage());

        // Then
        then(productRepository).should().findById(any());
    }

}
