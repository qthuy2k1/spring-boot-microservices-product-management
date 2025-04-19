package com.qthuy2k1.productservice.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.productservice.model.ProductCategoryModel;
import com.qthuy2k1.productservice.model.ProductModel;
import com.qthuy2k1.productservice.repository.ProductCategoryRepository;
import com.qthuy2k1.productservice.repository.ProductRepository;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@Testcontainers
@AutoConfigureMockMvc
public abstract class BaseControllerTest {
    static final int REDIS_PORT = 6379;
    @Container
    static final RedisContainer redisContainer =
            new RedisContainer(DockerImageName.parse("redis:5.0.3-alpine"))
                    .withExposedPorts(REDIS_PORT);
    static final int DEFAULT_CODE_RESPONSE = 1000;
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    ProductModel productSaved1;
    ProductModel productSaved2;
    ProductCategoryModel productCategorySaved;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // for postgres
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // for redis
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(REDIS_PORT));
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        productRepository.deleteAll();
        productCategoryRepository.deleteAll();

        productCategorySaved = productCategoryRepository.save(ProductCategoryModel.builder()
                .name("Category 1")
                .description("Description of Category 1")
                .build());
        productSaved1 = productRepository.save(ProductModel.builder()
                .name("Product 991")
                .description("Product description 991")
                .price(BigDecimal.valueOf(1000))
                .category(productCategorySaved)
                .skuCode("abc")
                .build());
        productSaved2 = productRepository.save(ProductModel.builder()
                .name("Product 992")
                .description("Product description 992")
                .price(BigDecimal.valueOf(2000))
                .category(productCategorySaved)
                .skuCode("abc")
                .build());
    }

    @Test
    void testContainersAreRunning() {
        assertThat(redisContainer.isRunning()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }
}