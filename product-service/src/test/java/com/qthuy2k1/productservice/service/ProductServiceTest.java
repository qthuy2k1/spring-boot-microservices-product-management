package com.qthuy2k1.productservice.service;

import com.qthuy2k1.productservice.dto.request.ProductRequest;
import com.qthuy2k1.productservice.dto.response.ProductResponse;
import com.qthuy2k1.productservice.enums.ErrorCode;
import com.qthuy2k1.productservice.mapper.ProductMapper;
import com.qthuy2k1.productservice.model.ProductCategoryModel;
import com.qthuy2k1.productservice.model.ProductModel;
import com.qthuy2k1.productservice.repository.ProductCategoryRepository;
import com.qthuy2k1.productservice.repository.ProductRepository;
import com.qthuy2k1.productservice.repository.feign.InventoryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductCategoryRepository productCategoryRepository;
    @Mock
    private InventoryClient inventoryClient;
    @InjectMocks
    private ProductService underTest;

    @BeforeEach
    void setUp() {
        underTest = new ProductService(productRepository, productCategoryRepository, inventoryClient, productMapper);
    }


    @Test
    void createProduct() {
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
        underTest.createProduct(productRequest);

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
        // When
        underTest.getAllProducts();

        // Then
        then(productRepository).should().findAll();
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
        underTest.deleteProductById(product.getId());

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
                underTest.deleteProductById(id))
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
        ProductResponse product = underTest.getProductById(id);

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
                underTest.getProductById(id))
                .hasMessageContaining(ErrorCode.PRODUCT_NOT_FOUND.getMessage());

        // Then
        then(productRepository).should().findById(any());
    }

}
