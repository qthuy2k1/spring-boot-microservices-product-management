package com.qthuy2k1.orderservice.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.orderservice.enums.OrderStatus;
import com.qthuy2k1.orderservice.model.OrderItemModel;
import com.qthuy2k1.orderservice.model.OrderModel;
import com.qthuy2k1.orderservice.repository.OrderItemRepository;
import com.qthuy2k1.orderservice.repository.OrderRepository;
import com.qthuy2k1.orderservice.service.OrderService;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@AutoConfigureMockMvc
public abstract class BaseControllerTest {
    static final int DEFAULT_CODE_RESPONSE = 1000;
    static final String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak:26.0";
    static final String POSTGRES_IMAGE = "postgres:16-alpine";
    static final String KAFKA_IMAGE = "confluentinc/cp-kafka:7.4.0";
    @Container
    static final KeycloakContainer keycloakContainer =
            new KeycloakContainer(KEYCLOAK_IMAGE).withRealmImportFile("keycloak/my-realm.json");
    @Container
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE);
    @Container
    static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE));
    @Autowired
    ObjectMapper objectMapper;
    OrderModel savedOrder;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderItemRepository orderItemRepository;
    @Autowired
    OrderService orderService;
    OrderItemModel savedOrderItem1;
    OrderItemModel savedOrderItem2;
    @LocalServerPort
    int serverPort;
    UUID userId = UUID.randomUUID();
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
        // for kafka
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        // for keycloak
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakContainer.getAuthServerUrl() + "/realms/my-realm");
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = serverPort;

        orderItemRepository.deleteAll();
        orderRepository.deleteAll();

        savedOrder = OrderModel.builder()
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(3000))
                .userId(userId)
                .build();
        savedOrderItem1 = OrderItemModel.builder()
                .price(BigDecimal.valueOf(1000))
                .productId(1)
                .quantity(1)
                .order(savedOrder)
                .build();
        savedOrderItem2 = OrderItemModel.builder()
                .price(BigDecimal.valueOf(2000))
                .productId(2)
                .quantity(2)
                .order(savedOrder)
                .build();
        orderItemRepository.saveAll(List.of(savedOrderItem1, savedOrderItem2));
        savedOrder.setOrderItems(Set.of(savedOrderItem1, savedOrderItem2));
        savedOrder = orderRepository.save(savedOrder);

        // set request context
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer dummy-token-for-tests");

        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @Test
    void testContainersAreRunning() {
        assertThat(postgresContainer.isRunning()).isTrue();
    }

    String getAdminToken() throws URISyntaxException {
        URI authorizationURI = new URIBuilder(
                keycloakContainer.getAuthServerUrl() + "/realms/" + clientId + "/protocol/openid-connect/token").build();
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