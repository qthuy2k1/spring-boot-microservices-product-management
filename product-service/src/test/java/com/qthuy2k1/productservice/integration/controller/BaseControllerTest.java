package com.qthuy2k1.productservice.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.productservice.model.ProductCategoryModel;
import com.qthuy2k1.productservice.model.ProductModel;
import com.qthuy2k1.productservice.repository.ProductCategoryRepository;
import com.qthuy2k1.productservice.repository.ProductRepository;
import com.redis.testcontainers.RedisContainer;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@AutoConfigureMockMvc
public abstract class BaseControllerTest {
    static final int REDIS_PORT = 6379;
    static final int DEFAULT_CODE_RESPONSE = 1000;
    static final String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak:26.0";
    static final String KEYCLOAK_REALM = "my-realm";
    static final String POSTGRES_IMAGE = "postgres:16-alpine";
    static final String REDIS_IMAGE = "redis:6.2-alpine";
    static final String KAFKA_IMAGE = "confluentinc/cp-kafka:7.4.0";
    @Container
    static final KeycloakContainer keycloakContainer =
            new KeycloakContainer(KEYCLOAK_IMAGE).withRealmImportFile("keycloak/my-realm.json");
    @Container
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE);
    @Container
    static final RedisContainer redisContainer =
            new RedisContainer(DockerImageName.parse(REDIS_IMAGE)).withExposedPorts(REDIS_PORT);
    @Container
    static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE));
    @Autowired
    ObjectMapper objectMapper;
    ProductModel productSaved1;
    ProductModel productSaved2;
    ProductCategoryModel productCategorySaved;
    @LocalServerPort
    int serverPort;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductCategoryRepository productCategoryRepository;
    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // for postgres
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        // for redis
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(REDIS_PORT));
        // for keycloak
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> keycloakContainer.getAuthServerUrl() + "/realms/my-realm");
        // for kafka
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        keycloakContainer.start();
        RestAssured.port = serverPort;

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
        assertThat(postgresContainer.isRunning()).isTrue();
        assertThat(keycloakContainer.isRunning()).isTrue();
        assertThat(kafkaContainer.isRunning()).isTrue();
    }

    String getAdminToken() throws URISyntaxException {
        URI authorizationURI = new URIBuilder(
                keycloakContainer.getAuthServerUrl() + "/realms/" + KEYCLOAK_REALM + "/protocol/openid-connect/token").build();
        String result = given()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", clientId)
                .formParam("client_secret", clientSecret)
                .formParam("username", "janedoe")
                .formParam("password", "s3cr3t")
                .formParam("scope", "openid")
                .when().post(authorizationURI)
                .then().statusCode(HttpStatus.OK.value())
                .extract().asString();

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return "Bearer " + jsonParser.parseMap(result)
                .get("access_token")
                .toString();
    }
}