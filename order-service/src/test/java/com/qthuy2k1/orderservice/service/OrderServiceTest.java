package com.qthuy2k1.orderservice.service;

import com.qthuy2k1.orderservice.dto.OrderItemRequest;
import com.qthuy2k1.orderservice.dto.OrderRequest;
import com.qthuy2k1.orderservice.event.OrderPlaced;
import com.qthuy2k1.orderservice.model.OrderModel;
import com.qthuy2k1.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemService orderItemService;
    @Mock
    @LoadBalanced
    private WebClient.Builder webClientBuilder;
    @Mock
    private KafkaTemplate<String, OrderPlaced> kafkaTemplate;
    private OrderService underTest;

    @BeforeEach
    void setUp() {

        underTest = new OrderService(orderRepository, orderItemService, kafkaTemplate, webClientBuilder);
    }

    @Test
    void createOrder() {
        Set<OrderItemRequest> orderItemRequestList = new HashSet<OrderItemRequest>();
        orderItemRequestList.add(
                OrderItemRequest.builder()
                        .price(BigDecimal.valueOf(1000))
                        .productId(1)
                        .build()
        );

        OrderRequest orderRequest = OrderRequest.builder()
                .userId(1)
                .status("PENDING")
                .orderItem(orderItemRequestList)
                .build();


        // When
        underTest.createOrder(orderRequest);

        // Then
        ArgumentCaptor<OrderModel> orderArgumentCaptor = ArgumentCaptor.forClass(OrderModel.class);
        verify(orderRepository).save(orderArgumentCaptor.capture());

        OrderModel capturedOrder = orderArgumentCaptor.getValue();

        assertThat(capturedOrder.getStatus()).isEqualTo(orderRequest.getStatus());
        assertThat(capturedOrder.getUserId()).isEqualTo(orderRequest.getUserId());
        assertThat(capturedOrder.getTotalAmount()).isEqualTo(BigDecimal.valueOf(1000));

    }

}
