package com.qthuy2k1.orderservice.service;

import com.qthuy2k1.orderservice.event.OrderPlaced;
import com.qthuy2k1.orderservice.mapper.OrderItemMapper;
import com.qthuy2k1.orderservice.mapper.OrderMapper;
import com.qthuy2k1.orderservice.repository.OrderItemRepository;
import com.qthuy2k1.orderservice.repository.OrderRepository;
import com.qthuy2k1.orderservice.repository.feign.InventoryClient;
import com.qthuy2k1.orderservice.repository.feign.ProductClient;
import com.qthuy2k1.orderservice.repository.feign.UserClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
//@WithMockUser(username = "admin@gmail.com", password = "password", roles = "ADMIN")
public class OrderServiceTest {
    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);
    private final OrderItemMapper orderItemMapper = Mappers.getMapper(OrderItemMapper.class);
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    @LoadBalanced
    private WebClient.Builder webClientBuilder;
    @Mock
    private KafkaTemplate<String, OrderPlaced> kafkaTemplate;
    @InjectMocks
    private OrderService underTest;
    @Mock
    private UserClient userClient;
    @Mock
    private ProductClient productClient;
    @Mock
    private InventoryClient inventoryClient;

    @BeforeEach
    void setUp() {
        underTest = new OrderService(
                orderRepository,
                orderItemRepository,
                kafkaTemplate,
                webClientBuilder,
                userClient,
                productClient,
                inventoryClient,
                orderMapper,
                orderItemMapper
        );
    }

//    @Test
//    void createOrder() {
//        Set<OrderItemRequest> orderItemRequestList = new HashSet<>();
//        orderItemRequestList.add(
//                OrderItemRequest.builder()
//                        .price(BigDecimal.valueOf(1000))
//                        .productId(1)
//                        .build()
//        );
//
//        OrderRequest orderRequest = OrderRequest.builder()
//                .status("PENDING")
//                .orderItem(orderItemRequestList)
//                .build();
//        OrderModel orderModel = orderMapper.toOder(orderRequest);
//
//
//        // When
//        when(orderRepository.save(any())).thenReturn(orderModel);
//        underTest.createOrder(orderRequest);
//
//        // Then
//        ArgumentCaptor<OrderModel> orderArgumentCaptor = ArgumentCaptor.forClass(OrderModel.class);
//        then(orderRepository).should().save(orderArgumentCaptor.capture());
//
//        OrderModel capturedOrder = orderArgumentCaptor.getValue();
//
//        assertThat(capturedOrder.getStatus()).isEqualTo(orderRequest.getStatus());
//        assertThat(capturedOrder.getTotalAmount()).isEqualTo(BigDecimal.valueOf(1000));
//    }
}
