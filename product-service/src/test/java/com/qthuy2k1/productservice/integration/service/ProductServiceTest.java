package com.qthuy2k1.productservice.integration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qthuy2k1.productservice.dto.request.ProductRequest;
import com.qthuy2k1.productservice.dto.response.PaginatedResponse;
import com.qthuy2k1.productservice.dto.response.ProductResponse;
import com.qthuy2k1.productservice.enums.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(properties = "spring.profiles.active=test")
@DirtiesContext
public class ProductServiceTest extends BaseServiceTest {
    private final DecimalFormat decimalFormatPrice = new DecimalFormat("#.00");
    int defaultPage = 0;
    int defaultSize = 10;

    @Test
    void create_And_GetAll_Product() throws JsonProcessingException {
        // Given
        ProductRequest productRequest1 = ProductRequest.builder()
                .name("Product 1")
                .description("Product description 1")
                .price(BigDecimal.valueOf(1))
                .categoryId(productCategorySaved.getId())
                .skuCode("abc")
                .quantity(1)
                .build();
        ProductRequest productRequest2 = ProductRequest.builder()
                .name("Product 2")
                .description("Product description 2")
                .price(BigDecimal.valueOf(1))
                .categoryId(productCategorySaved.getId())
                .skuCode("abc")
                .quantity(1)
                .build();

        ProductResponse productCreated1 = productService.createProduct(productRequest1);
        ProductResponse productCreated2 = productService.createProduct(productRequest2);

        PaginatedResponse<ProductResponse> products = productService.getAllProducts(defaultPage, defaultSize);
        // Get the newly inserted product which is at index 1
        ProductResponse productResp1 = products.getData().get(1);
        ProductResponse productResp2 = products.getData().get(2);

        // The product list size should be 3 (1 existing product plus 2 newly inserted products)
        assertThat(products.getPagination().getTotalRecords()).isEqualTo(3);
        assertThat(productResp1.getName()).isEqualTo(productCreated1.getName());
        assertThat(productResp1.getDescription()).isEqualTo(productCreated1.getDescription());
        assertThat(productResp1.getPrice()).isEqualTo(decimalFormatPrice.format(productCreated1.getPrice()));
        assertThat(productResp1.getCategory().getId()).isEqualTo(productCreated1.getCategory().getId());
        assertThat(productResp1.getSkuCode()).isEqualTo(productCreated1.getSkuCode());

        assertThat(productResp2.getName()).isEqualTo(productCreated2.getName());
        assertThat(productResp2.getDescription()).isEqualTo(productCreated2.getDescription());
        assertThat(productResp2.getPrice()).isEqualTo(decimalFormatPrice.format(productCreated2.getPrice()));
        assertThat(productResp2.getCategory().getId()).isEqualTo(productCreated2.getCategory().getId());
        assertThat(productResp2.getSkuCode()).isEqualTo(productCreated2.getSkuCode());
    }

    @Test
    void getAllProducts() {
        PaginatedResponse<ProductResponse> products = productService.getAllProducts(defaultPage, defaultSize);
        // Get the inserted product which is at index 0
        ProductResponse productResp1 = products.getData().getFirst();

        // The product list size should be 1 (1 existing product)
        assertThat(products.getPagination().getTotalRecords()).isEqualTo(1);
        assertThat(productResp1.getName()).isEqualTo(productSaved.getName());
        assertThat(productResp1.getDescription()).isEqualTo(productSaved.getDescription());
        assertThat(productResp1.getPrice()).isEqualTo(decimalFormatPrice.format(productSaved.getPrice()));
        assertThat(productResp1.getCategory().getId()).isEqualTo(productSaved.getCategory().getId());
        assertThat(productResp1.getSkuCode()).isEqualTo(productSaved.getSkuCode());
    }

    @Test
    void deleteProductById() {
        // delete the existing product in db
        productService.deleteProductById(productSaved.getId());

        PaginatedResponse<ProductResponse> products = productService.getAllProducts(defaultPage, defaultSize);
        // The product list size should be 0
        assertThat(products.getPagination().getTotalRecords()).isEqualTo(0);
    }

    @Test
    void deleteProductById_ExceptionThrown_ProductNotFound() {
        int id = productSaved.getId() + 1; // Ensure a non-existent ID
        assertThatThrownBy(() ->
                productService.deleteProductById(id))
                .hasMessageContaining(ErrorCode.PRODUCT_NOT_FOUND.getMessage());

    }

    @Test
    void getProductById() {
        ProductResponse product = productService.getProductById(productSaved.getId());

        assertThat(product.getId()).isEqualTo(productSaved.getId());
        assertThat(product.getName()).isEqualTo(productSaved.getName());
        assertThat(product.getDescription()).isEqualTo(productSaved.getDescription());
        assertThat(product.getPrice()).isEqualTo(decimalFormatPrice.format(productSaved.getPrice()));
        assertThat(product.getSkuCode()).isEqualTo(productSaved.getSkuCode());
    }


    @Test
    void getProductById_ExceptionThrown_ProductNotFound() {
        int id = productSaved.getId() + 1; // Ensure a non-existent ID
        assertThatThrownBy(() ->
                productService.getProductById(id))
                .hasMessageContaining(ErrorCode.PRODUCT_NOT_FOUND.getMessage());
    }

}
