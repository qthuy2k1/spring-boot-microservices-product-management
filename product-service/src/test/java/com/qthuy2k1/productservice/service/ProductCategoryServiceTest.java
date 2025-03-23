package com.qthuy2k1.productservice.service;

import com.qthuy2k1.productservice.dto.request.ProductCategoryRequest;
import com.qthuy2k1.productservice.dto.response.ProductCategoryResponse;
import com.qthuy2k1.productservice.mapper.ProductCategoryMapper;
import com.qthuy2k1.productservice.model.ProductCategoryModel;
import com.qthuy2k1.productservice.repository.ProductCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
@Testcontainers
public class ProductCategoryServiceTest extends AbstractIntegrationTest {
    private final ProductCategoryMapper productCategoryMapper = Mappers.getMapper(ProductCategoryMapper.class);
    @Autowired
    private ProductCategoryRepository productCategoryRepository;
    @Autowired
    private IProductCategoryService productCategoryService;
    private ProductCategoryModel productCategorySaved;

    @BeforeEach
    void setup() {
        productCategoryRepository.deleteAll();
        productCategorySaved = productCategoryRepository.save(ProductCategoryModel.builder()
                .name("Category 999")
                .description("Description of Category 999")
                .products(Set.of())
                .build());
    }

    @Test
    void create_And_GetAll_ProductCategory() {
        // given
        ProductCategoryRequest productCategoryRequest1 = ProductCategoryRequest.builder()
                .name("Product Category 1")
                .description("Product category description 1")
                .build();
        ProductCategoryRequest productCategoryRequest2 = ProductCategoryRequest.builder()
                .name("Product Category 2")
                .description("Product category description 2")
                .build();

        ProductCategoryResponse productCategoryCreate1 = productCategoryService.createProductCategory(productCategoryRequest1);
        ProductCategoryResponse productCategoryCreate2 = productCategoryService.createProductCategory(productCategoryRequest2);

        List<ProductCategoryResponse> productCategories = productCategoryService.getAllProductCategories();
        // Get the newly inserted product category which is at index 1
        ProductCategoryResponse productCategoryResp1 = productCategories.get(1);
        ProductCategoryResponse productCategoryResp2 = productCategories.get(2);
        assertThat(productCategories.size()).isEqualTo(3);
        assertThat(productCategoryResp1.getName()).isEqualTo(productCategoryCreate1.getName());
        assertThat(productCategoryResp1.getDescription()).isEqualTo(productCategoryCreate1.getDescription());
        assertThat(productCategoryResp2.getName()).isEqualTo(productCategoryCreate2.getName());
        assertThat(productCategoryResp2.getDescription()).isEqualTo(productCategoryCreate2.getDescription());
    }

    @Test
    void getProductCategoryById() {
        ProductCategoryResponse productCategoryResponse = productCategoryService.getProductCategoryById(productCategorySaved.getId());

        assertThat(productCategoryResponse.getId()).isEqualTo(productCategorySaved.getId());
        assertThat(productCategoryResponse.getName()).isEqualTo(productCategorySaved.getName());
        assertThat(productCategoryResponse.getDescription()).isEqualTo(productCategorySaved.getDescription());
    }
}
