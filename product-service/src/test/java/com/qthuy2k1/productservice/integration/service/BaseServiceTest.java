package com.qthuy2k1.productservice.integration.service;

import com.qthuy2k1.productservice.model.ProductCategoryModel;
import com.qthuy2k1.productservice.model.ProductModel;
import com.qthuy2k1.productservice.repository.ProductCategoryRepository;
import com.qthuy2k1.productservice.repository.ProductRepository;
import com.qthuy2k1.productservice.service.ProductService;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Testcontainers
public abstract class BaseServiceTest {

    protected static final int REDIS_PORT = 6379;
    @Container
    protected static final RedisContainer redisContainer =
            new RedisContainer(DockerImageName.parse("redis:6.2-alpine")).withExposedPorts(REDIS_PORT);
    @Container
    protected static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );
    @Container
    protected static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));
    ProductModel productSaved;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ProductCategoryRepository productCategoryRepository;
    @Autowired
    ProductService productService;
    ProductCategoryModel productCategorySaved;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // for postgres
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        // for redis
        // for spring boot 3 must be spring.data.redis
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(REDIS_PORT));

        // for kafka
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        productCategoryRepository.deleteAll();

        productCategorySaved = productCategoryRepository.save(ProductCategoryModel.builder()
                .name("Category 1")
                .description("Description of Category 1")
                .products(Set.of())
                .build());
        productSaved = productRepository.save(ProductModel.builder()
                .name("Product 999")
                .description("Product description 999")
                .price(BigDecimal.valueOf(1))
                .category(productCategorySaved)
                .skuCode("abc")
                .build());
    }

    @Test
    public void testConnection() {
        assertThat(postgresContainer.isRunning()).isTrue();
        assertThat(redisContainer.isRunning()).isTrue();
        assertThat(kafkaContainer.isRunning()).isTrue();
    }
}