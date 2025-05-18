package com.qthuy2k1.orderservice.unit.service;

import com.qthuy2k1.orderservice.dto.request.OrderItemRequest;
import com.qthuy2k1.orderservice.dto.request.OrderRequest;
import com.qthuy2k1.orderservice.dto.request.ReduceInventoryRequest;
import com.qthuy2k1.orderservice.dto.request.UpdateStatusOrderRequest;
import com.qthuy2k1.orderservice.dto.response.*;
import com.qthuy2k1.orderservice.enums.OrderStatus;
import com.qthuy2k1.orderservice.event.OrderPlaced;
import com.qthuy2k1.orderservice.exception.AppException;
import com.qthuy2k1.orderservice.mapper.OrderItemMapper;
import com.qthuy2k1.orderservice.mapper.OrderMapper;
import com.qthuy2k1.orderservice.model.OrderItemModel;
import com.qthuy2k1.orderservice.model.OrderModel;
import com.qthuy2k1.orderservice.model.ProductReportList;
import com.qthuy2k1.orderservice.model.ReportModel;
import com.qthuy2k1.orderservice.repository.OrderItemRepository;
import com.qthuy2k1.orderservice.repository.OrderReportRepository;
import com.qthuy2k1.orderservice.repository.OrderRepository;
import com.qthuy2k1.orderservice.repository.feign.InventoryClient;
import com.qthuy2k1.orderservice.repository.feign.ProductClient;
import com.qthuy2k1.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@WithMockUser(username = "admin@gmail.com", roles = {"admin"})
public class OrderServiceTest {
    UUID userId = UUID.randomUUID();
    @Mock
    WebClient.ResponseSpec responseSpec;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    @LoadBalanced
    private WebClient.Builder webClientBuilder;
    @Mock
    private KafkaTemplate<String, OrderPlaced> orderPlacedKafkaTemplate;
    @Mock
    private KafkaTemplate<String, List<ReduceInventoryRequest>> reduceInventoryKafkaTemplate;
    @InjectMocks
    private OrderService orderService;
    @Mock
    private ProductClient productClient;
    @Mock
    private InventoryClient inventoryClient;
    @Mock
    private OrderReportRepository orderReportRepository;
    @Mock
    private WebClient userClient;
    @Mock
    private WebClient.RequestHeadersUriSpec uriSpec;
    @Mock
    private WebClient.RequestBodyUriSpec headerSpec;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                orderItemRepository,
                orderReportRepository,
                orderPlacedKafkaTemplate,
                reduceInventoryKafkaTemplate,
                webClientBuilder,
                productClient,
                inventoryClient,
                orderMapper,
                orderItemMapper,
                userClient
        );

        // set request context
        MockHttpServletRequest request = new MockHttpServletRequest();
        // Optionally set headers if your code needs them
        request.addHeader("Authorization", "Bearer dummy-token-for-tests");

        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @Test
    void createOrder_shouldReturnOrderResponse() throws Exception {
        // Given
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setStatus("PENDING");

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1);
        itemRequest.setQuantity(2);
        orderRequest.setOrderItem(Set.of(itemRequest));

        ProductResponse product = new ProductResponse();
        product.setId(1);
        product.setPrice(String.valueOf(10.0));
        product.setName("Test Product");
        product.setDescription("Test Product Description");

        UserResponse user = new UserResponse();
        user.setId(userId);

        ApiResponse<List<ProductResponse>> productApiResponse = new ApiResponse<>();
        productApiResponse.setResult(List.of(product));

        when(userClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headerSpec);
        when(headerSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(headerSpec);
        when(headerSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserResponse.class)).thenReturn(Mono.just(user));

        when(productClient.getProductsByListId("1")).thenReturn(productApiResponse);
        when(inventoryClient.isInStock(2, 1)).thenReturn(new InventoryResponse(true));

        OrderModel orderModel = new OrderModel();
        orderModel.setId(1);
        orderModel.setStatus(OrderStatus.PENDING);
        orderModel.setUserId(userId);

        OrderItemModel itemModel = new OrderItemModel();
        itemModel.setProductId(1);
        itemModel.setPrice(BigDecimal.valueOf(10.0));
        itemModel.setQuantity(2);

        OrderResponse mockResponse = new OrderResponse();
        mockResponse.setId(1);
        mockResponse.setStatus("PENDING");
        mockResponse.setTotalAmount(BigDecimal.valueOf(20.0));
        mockResponse.setUserId(userId.toString());

        when(orderMapper.toOder(any(OrderRequest.class))).thenReturn(orderModel);
        when(orderRepository.save(any())).thenReturn(orderModel);
        when(orderMapper.toOrderResponse(any())).thenReturn(mockResponse);

        OrderResponse result = orderService.createOrder(orderRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("PENDING", result.getStatus());
        assertEquals(BigDecimal.valueOf(20.0), result.getTotalAmount());
        assertEquals(userId.toString(), result.getUserId());

        verify(productClient).getProductsByListId("1");
        verify(inventoryClient).isInStock(2, 1);
        verify(orderRepository).save(any(OrderModel.class));
        verify(orderMapper).toOrderResponse(any());
    }

    @Test
    void updateOrder_shouldUpdateOrderStatus_whenOrderExists() {
        // Given
        Integer orderId = 1;
        UpdateStatusOrderRequest orderRequest = new UpdateStatusOrderRequest();
        orderRequest.setStatus(OrderStatus.DELIVERED.getLabel());

        OrderModel existingOrder = new OrderModel();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.PENDING);
        existingOrder.setTotalAmount(BigDecimal.valueOf(100));
        existingOrder.setUserId(userId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(OrderModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        orderService.updateStatusOrder(orderId, orderRequest);

        // Then
        assertNotNull(existingOrder);
        assertEquals(OrderStatus.DELIVERED, existingOrder.getStatus());
        assertEquals(orderId, existingOrder.getId());
        assertEquals(userId, existingOrder.getUserId());
        assertEquals(BigDecimal.valueOf(100), existingOrder.getTotalAmount());
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(existingOrder);
    }

    @Test
    void updateOrder_shouldThrowException_whenOrderNotFound() {
        // Given
        Integer orderId = 99;
        UpdateStatusOrderRequest orderRequest = new UpdateStatusOrderRequest();
        orderRequest.setStatus(OrderStatus.CANCELED.getLabel());

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Then
        assertThrows(AppException.class, () -> orderService.updateStatusOrder(orderId, orderRequest));
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getReport_shouldReturnReportResponseWithTopSellingProducts() {
        // Given
        String startDate = "2024-01-01 00:00:00";
        String endDate = "2024-01-31 00:00:00";

        ReportModel mockReport = new ReportModel();
        mockReport.setPeriod(30);
        mockReport.setTotalorders(100L);
        mockReport.setAvgordervalue(BigDecimal.valueOf(50.5));
        mockReport.setNewcustomers(20L);
        mockReport.setReturningcustomers(80L);
        mockReport.setPending(10L);
        mockReport.setShipped(30L);
        mockReport.setProcessing(20L);
        mockReport.setDelivered(30L);
        mockReport.setCanceled(10L);

        ProductReportList reportProduct1 = new ProductReportList();
        reportProduct1.setProduct_id(1);

        ProductResponse product1 = new ProductResponse();
        product1.setId(1);
        product1.setName("Product 1");

        ApiResponse<List<ProductResponse>> productApiResponse = new ApiResponse<>();
        productApiResponse.setResult(List.of(product1));

        when(orderReportRepository.getOrderReport(startDate, endDate)).thenReturn(mockReport);
        when(orderReportRepository.getProductReportList()).thenReturn(List.of(reportProduct1));
        when(productClient.getProductsByListId("1")).thenReturn(productApiResponse);

        // When
        ReportResponse result = orderService.getReport(startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReportPeriod()).isEqualTo("30 day(s)");
        assertThat(result.getTotalOrders()).isEqualTo(100);
        assertThat(result.getAvgOrderValue()).isEqualTo("50.50");
        assertThat(result.getTopSellingProducts()).hasSize(1);
        assertThat(result.getTopSellingProducts().getFirst().getName()).isEqualTo("Product 1");
        assertThat(result.getNewCustomers()).isEqualTo(20);
        assertThat(result.getReturningCustomers()).isEqualTo(80);
        assertThat(result.getPending()).isEqualTo(10);
        assertThat(result.getShipping()).isEqualTo(30);
        assertThat(result.getProcessing()).isEqualTo(20);
        assertThat(result.getDelivered()).isEqualTo(30);
        assertThat(result.getCanceled()).isEqualTo(10);


        verify(orderReportRepository).getOrderReport(startDate, endDate);
        verify(orderReportRepository).getProductReportList();
        verify(productClient).getProductsByListId("1");
    }
}
