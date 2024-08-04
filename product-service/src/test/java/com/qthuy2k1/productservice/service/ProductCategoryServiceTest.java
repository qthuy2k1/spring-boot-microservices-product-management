package com.qthuy2k1.productservice.service;

import com.qthuy2k1.productservice.dto.request.ProductCategoryRequest;
import com.qthuy2k1.productservice.dto.response.ProductCategoryResponse;
import com.qthuy2k1.productservice.mapper.ProductCategoryMapper;
import com.qthuy2k1.productservice.model.ProductCategoryModel;
import com.qthuy2k1.productservice.repository.ProductCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
public class ProductCategoryServiceTest {
    private final ProductCategoryMapper productCategoryMapper = Mappers.getMapper(ProductCategoryMapper.class);
    @Mock
    private ProductCategoryRepository productCategoryRepository;
    @InjectMocks
    private ProductCategoryService underTest;

    @BeforeEach
    void setup() {
        underTest = new ProductCategoryService(productCategoryRepository, productCategoryMapper);
    }

    @Test
    void create() {
        // given
        ProductCategoryRequest productCategoryRequest = ProductCategoryRequest.builder()
                .name("Product Category")
                .description("Product category description")
                .build();
        ProductCategoryModel productCategory = productCategoryMapper.toProductCategory(productCategoryRequest);

        // when
        when(productCategoryRepository.save(any())).thenReturn(productCategory);
        underTest.createProductCategory(productCategoryRequest);

        // Then
        ArgumentCaptor<ProductCategoryModel> productCategoryArgumentCaptor = ArgumentCaptor.forClass(ProductCategoryModel.class);
        then(productCategoryRepository).should().save(productCategoryArgumentCaptor.capture());

        ProductCategoryModel capturedProductCategory = productCategoryArgumentCaptor.getValue();

        assertThat(capturedProductCategory.getId()).isEqualTo(productCategory.getId());
        assertThat(capturedProductCategory.getName()).isEqualTo(productCategory.getName());
        assertThat(capturedProductCategory.getDescription()).isEqualTo(productCategory.getDescription());
    }

    @Test
    void getAll() {
        // when
        underTest.getAllProductCategories();

        // then
        then(productCategoryRepository).should().findAll();
    }

    @Test
    void getProductCategoryById() {
        // given
        int id = 1;
        ProductCategoryModel productCategoryModel = ProductCategoryModel.builder()
                .id(1)
                .name("category 1")
                .description("description 1")
                .build();
        given(productCategoryRepository.findById(id)).willReturn(Optional.of(productCategoryModel));

        // When
        ProductCategoryResponse productCategoryResponse = underTest.getProductCategoryById(id);

        // Then
        then(productCategoryRepository).should().findById(id);

        assertThat(productCategoryResponse.getId()).isEqualTo(productCategoryModel.getId());
        assertThat(productCategoryResponse.getName()).isEqualTo(productCategoryModel.getName());
        assertThat(productCategoryResponse.getDescription()).isEqualTo(productCategoryModel.getDescription());
    }
}
