package com.qthuy2k1.orderservice.integration.service;

import com.qthuy2k1.orderservice.enums.OrderStatus;
import com.qthuy2k1.orderservice.model.OrderItemModel;
import com.qthuy2k1.orderservice.model.OrderModel;
import com.qthuy2k1.orderservice.repository.OrderItemRepository;
import com.qthuy2k1.orderservice.repository.OrderRepository;
import com.qthuy2k1.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public abstract class BaseServiceTest {
    static final String POSTGRES_IMAGE = "postgres:16-alpine";
    static final String KAFKA_IMAGE = "confluentinc/cp-kafka:7.4.0";
    @Container
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE);
    @Container
    static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE));
    OrderModel savedOrder;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderItemRepository orderItemRepository;
    @Autowired
    OrderService orderService;
    OrderItemModel savedOrderItem1;
    OrderItemModel savedOrderItem2;
    UUID userId = UUID.randomUUID();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // for postgres
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        // for kafka
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
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
    public void testConnection() {
        assertTrue(postgresContainer.isRunning());
        assertTrue(kafkaContainer.isRunning());
    }
}