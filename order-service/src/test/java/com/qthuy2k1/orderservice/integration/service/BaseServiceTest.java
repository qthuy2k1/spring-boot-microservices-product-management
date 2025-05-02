package com.qthuy2k1.orderservice.integration.service;

import com.qthuy2k1.orderservice.model.OrderItemModel;
import com.qthuy2k1.orderservice.model.OrderModel;
import com.qthuy2k1.orderservice.repository.OrderItemRepository;
import com.qthuy2k1.orderservice.repository.OrderRepository;
import com.qthuy2k1.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public abstract class BaseServiceTest {
    @Container
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );
    @Container
    static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));
    OrderModel savedOrder;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderItemRepository orderItemRepository;
    @Autowired
    OrderService orderService;
    OrderItemModel savedOrderItem1;
    OrderItemModel savedOrderItem2;

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
                .status("PENDING")
                .totalAmount(BigDecimal.valueOf(3000))
                .userId(1)
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
    }

    @Test
    public void testConnection() {
        assertTrue(postgresContainer.isRunning());
        assertTrue(kafkaContainer.isRunning());
    }
}