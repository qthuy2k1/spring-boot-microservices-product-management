package com.qthuy2k1.productservice.integration.service;

import com.qthuy2k1.productservice.dto.request.ProductRequest;
import com.qthuy2k1.productservice.dto.response.ProductResponse;
import com.qthuy2k1.productservice.enums.ErrorCode;
import com.qthuy2k1.productservice.model.ProductCategoryModel;
import com.qthuy2k1.productservice.model.ProductModel;
import com.qthuy2k1.productservice.repository.ProductCategoryRepository;
import com.qthuy2k1.productservice.repository.ProductRepository;
import com.qthuy2k1.productservice.repository.feign.InventoryClient;
import com.qthuy2k1.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
public class ProductServiceTest extends BaseServiceTest {
    private final DecimalFormat decimalFormatPrice = new DecimalFormat("#.00");
    private ProductModel productSaved;
    private Integer productCategorySavedId;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductCategoryRepository productCategoryRepository;
    @MockBean
    private InventoryClient inventoryClient;
    @Autowired
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        ProductCategoryModel productCategoryModel = productCategoryRepository.save(ProductCategoryModel.builder()
                .name("Category 1")
                .description("Description of Category 1")
                .products(Set.of())
                .build());
        productSaved = productRepository.save(ProductModel.builder()
                .name("Product 999")
                .description("Product description 999")
                .price(BigDecimal.valueOf(1))
                .category(productCategoryModel)
                .skuCode("abc")
                .build());

        productCategorySavedId = productCategoryModel.getId();
    }

    @Test
    public void testConnection() {
        assertThat(postgres.isRunning()).isTrue();
        assertThat(REDIS_CONTAINER.isRunning()).isTrue();
    }


    @Test
    void create_And_GetAll_Product() {
        // Given
        ProductRequest productRequest1 = ProductRequest.builder()
                .name("Product 1")
                .description("Product description 1")
                .price(BigDecimal.valueOf(1))
                .categoryId(productCategorySavedId)
                .skuCode("abc")
                .quantity(1)
                .build();
        ProductRequest productRequest2 = ProductRequest.builder()
                .name("Product 2")
                .description("Product description 2")
                .price(BigDecimal.valueOf(1))
                .categoryId(productCategorySavedId)
                .skuCode("abc")
                .quantity(1)
                .build();

        ProductResponse productCreate1 = productService.createProduct(productRequest1);
        ProductResponse productCreate2 = productService.createProduct(productRequest2);

        List<ProductResponse> products = productService.getAllProducts();
        // Get the newly inserted product which is at index 1
        ProductResponse productResp1 = products.get(1);
        ProductResponse productResp2 = products.get(2);

        // The product list size should be 3 (1 existing product plus 2 newly inserted products)
        assertThat(products.size()).isEqualTo(3);
        assertThat(productResp1.getName()).isEqualTo(productCreate1.getName());
        assertThat(productResp1.getDescription()).isEqualTo(productCreate1.getDescription());
        assertThat(productResp1.getPrice()).isEqualTo(decimalFormatPrice.format(productCreate1.getPrice()));
        assertThat(productResp1.getCategory().getId()).isEqualTo(productCreate1.getCategory().getId());
        assertThat(productResp1.getSkuCode()).isEqualTo(productCreate1.getSkuCode());
        assertThat(productResp2.getName()).isEqualTo(productCreate2.getName());
        assertThat(productResp2.getDescription()).isEqualTo(productCreate2.getDescription());
        assertThat(productResp2.getPrice()).isEqualTo(decimalFormatPrice.format(productCreate2.getPrice()));
        assertThat(productResp2.getCategory().getId()).isEqualTo(productCreate2.getCategory().getId());
        assertThat(productResp2.getSkuCode()).isEqualTo(productCreate2.getSkuCode());
    }

    @Test
    void deleteProductById() {
        // delete the existing product in db
        productService.deleteProductById(productSaved.getId());

        List<ProductResponse> products = productService.getAllProducts();
        // The product list size should be 0
        assertThat(products.size()).isEqualTo(0);
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
