package com.qthuy2k1.orderservice.integration.service;

import com.qthuy2k1.orderservice.dto.request.OrderItemRequest;
import com.qthuy2k1.orderservice.dto.request.OrderRequest;
import com.qthuy2k1.orderservice.dto.response.*;
import com.qthuy2k1.orderservice.enums.ErrorCode;
import com.qthuy2k1.orderservice.repository.feign.InventoryClient;
import com.qthuy2k1.orderservice.repository.feign.ProductClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.profiles.active=test")
@DirtiesContext()
@WithMockUser(username = "test", roles = "admin")
public class OrderServiceTest extends BaseServiceTest {
    @MockBean
    InventoryClient inventoryClient;
    @MockBean
    ProductClient productClient;
    @MockBean
    WebClient userClient;
    @Mock
    WebClient.ResponseSpec responseSpec;
    @Mock
    private WebClient.RequestHeadersUriSpec uriSpec;
    @Mock
    private WebClient.RequestBodyUriSpec headerSpec;

    @Test
    void createOrder() throws ExecutionException, InterruptedException {
        OrderItemRequest orderItemRequest1 = OrderItemRequest.builder()
                .price(BigDecimal.valueOf(1000))
                .productId(1)
                .quantity(1)
                .build();

        OrderItemRequest orderItemRequest2 = OrderItemRequest.builder()
                .price(BigDecimal.valueOf(2000))
                .productId(2)
                .quantity(2)
                .build();
        OrderRequest orderRequest = OrderRequest.builder()
                .orderItem(Set.of(orderItemRequest1, orderItemRequest2))
                .build();
        ProductResponse product1 = new ProductResponse();
        product1.setId(1);
        product1.setPrice(String.valueOf(1000));
        product1.setName("Product Test 1");
        product1.setDescription("Product Test Description 1");
        ProductResponse product2 = new ProductResponse();
        product2.setId(2);
        product2.setPrice(String.valueOf(2000));
        product2.setName("Product Test 2");
        product2.setDescription("Product Test Description 2");
        ApiResponse<List<ProductResponse>> productApiResponse = ApiResponse.<List<ProductResponse>>builder()
                .result(List.of(product1, product2))
                .build();

        UserResponse user = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test Name")
                .build();

        when(userClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headerSpec);
        when(headerSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(headerSpec);
        when(headerSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserResponse.class)).thenReturn(Mono.just(user));

        when(productClient.getProductsByListId(any())).thenReturn(productApiResponse);
        when(inventoryClient.isInStock(any(), any())).thenReturn(new InventoryResponse(true));

        OrderResponse createdOrder = orderService.createOrder(orderRequest);

        assertThat(createdOrder)
                .as("Check that the created order is not null")
                .isNotNull();
        assertThat(createdOrder.getTotalAmount())
                .as("Check that the total amount of the created order is correct")
                .isEqualTo(orderItemRequest1.getPrice().multiply(BigDecimal.valueOf(orderItemRequest1.getQuantity()))
                        .add(orderItemRequest2.getPrice().multiply(BigDecimal.valueOf(orderItemRequest2.getQuantity()))));
        assertThat(createdOrder.getStatus())
                .as("Check that the order status is PENDING")
                .isEqualTo("PENDING");
        assertThat(createdOrder.getUserId())
                .as("Check that the user ID of the created order is correct")
                .isEqualTo(userId.toString());
        assertThat(createdOrder.getOrderItems().size())
                .as("Check that the number of order items is correct")
                .isEqualTo(2);

        List<OrderItemResponse> createdOrderItemList = createdOrder.getOrderItems().stream().sorted(Comparator.comparing(OrderItemResponse::getProductId)).toList();
        OrderItemResponse createdOrderItem1 = createdOrderItemList.getFirst();
        OrderItemResponse createdOrderItem2 = createdOrderItemList.getLast();

        assertThat(createdOrderItem1.getPrice())
                .as("Check that the price of the first order item matches the expected price")
                .isEqualTo(orderItemRequest1.getPrice());
        assertThat(createdOrderItem1.getQuantity())
                .as("Check that the quantity of the first order item matches the expected quantity")
                .isEqualTo(orderItemRequest1.getQuantity());
        assertThat(createdOrderItem1.getProductId())
                .as("Check that the product ID of the first order item matches the expected product ID")
                .isEqualTo(orderItemRequest1.getProductId());
        assertThat(createdOrderItem2.getPrice())
                .as("Check that the price of the second order item matches the expected price")
                .isEqualTo(orderItemRequest2.getPrice());
        assertThat(createdOrderItem2.getQuantity())
                .as("Check that the quantity of the second order item matches the expected quantity")
                .isEqualTo(orderItemRequest2.getQuantity());
        assertThat(createdOrderItem2.getProductId())
                .as("Check that the product ID of the second order item matches the expected product ID")
                .isEqualTo(orderItemRequest2.getProductId());
    }

    @Test
    void createOrder_ServiceUnavailable() {
        OrderItemRequest orderItemRequest1 = OrderItemRequest.builder()
                .price(BigDecimal.valueOf(1000))
                .productId(1)
                .quantity(1)
                .build();

        OrderItemRequest orderItemRequest2 = OrderItemRequest.builder()
                .price(BigDecimal.valueOf(2000))
                .productId(2)
                .quantity(2)
                .build();
        OrderRequest orderRequest = OrderRequest.builder()
                .orderItem(Set.of(orderItemRequest1, orderItemRequest2))
                .build();

        assertThatThrownBy(() -> orderService.createOrder(orderRequest)).hasMessageContaining(ErrorCode.SERVICE_UNAVAILABLE.getMessage());
    }

    @Test
    void createOrder_ProductNotFound() {
        OrderItemRequest orderItemRequest1 = OrderItemRequest.builder()
                .price(BigDecimal.valueOf(1000))
                .productId(1)
                .quantity(1)
                .build();

        OrderItemRequest orderItemRequest2 = OrderItemRequest.builder()
                .price(BigDecimal.valueOf(2000))
                .productId(2)
                .quantity(2)
                .build();
        OrderRequest orderRequest = OrderRequest.builder()
                .orderItem(Set.of(orderItemRequest1, orderItemRequest2))
                .build();
        ApiResponse<List<ProductResponse>> productApiResponse = ApiResponse.<List<ProductResponse>>builder()
                .result(null)
                .build();

        UserResponse user = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test Name")
                .build();
        ApiResponse<UserResponse> userApiResponse = new ApiResponse<>();
        userApiResponse.setResult(user);

        when(productClient.getProductsByListId(any())).thenReturn(productApiResponse);
        when(inventoryClient.isInStock(any(), any())).thenReturn(new InventoryResponse(true));

        assertThatThrownBy(() -> orderService.createOrder(orderRequest)).hasMessageContaining(ErrorCode.PRODUCT_NOT_FOUND.getMessage());
    }

    @Test
    void createOrder_UserNotFound() {
        OrderItemRequest orderItemRequest1 = OrderItemRequest.builder()
                .price(BigDecimal.valueOf(1000))
                .productId(1)
                .quantity(1)
                .build();

        OrderItemRequest orderItemRequest2 = OrderItemRequest.builder()
                .price(BigDecimal.valueOf(2000))
                .productId(2)
                .quantity(2)
                .build();
        OrderRequest orderRequest = OrderRequest.builder()
                .orderItem(Set.of(orderItemRequest1, orderItemRequest2))
                .build();
        ProductResponse product1 = new ProductResponse();
        product1.setId(1);
        product1.setPrice(String.valueOf(1000));
        product1.setName("Product Test 1");
        product1.setDescription("Product Test Description 1");
        ProductResponse product2 = new ProductResponse();
        product2.setId(2);
        product2.setPrice(String.valueOf(2000));
        product2.setName("Product Test 2");
        product2.setDescription("Product Test Description 2");
        ApiResponse<List<ProductResponse>> productApiResponse = ApiResponse.<List<ProductResponse>>builder()
                .result(List.of(product1, product2))
                .build();

        when(userClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headerSpec);
        when(headerSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(headerSpec);
        when(headerSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserResponse.class)).thenReturn(Mono.empty());

        when(productClient.getProductsByListId(any())).thenReturn(productApiResponse);
        when(inventoryClient.isInStock(any(), any())).thenReturn(new InventoryResponse(true));

        assertThatThrownBy(() -> orderService.createOrder(orderRequest)).hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void createOrder_ProductOutOfStock() {
        OrderItemRequest orderItemRequest1 = OrderItemRequest.builder()
                .price(BigDecimal.valueOf(1000))
                .productId(1)
                .quantity(1)
                .build();

        OrderItemRequest orderItemRequest2 = OrderItemRequest.builder()
                .price(BigDecimal.valueOf(2000))
                .productId(2)
                .quantity(2)
                .build();
        OrderRequest orderRequest = OrderRequest.builder()
                .orderItem(Set.of(orderItemRequest1, orderItemRequest2))
                .build();
        ProductResponse product1 = new ProductResponse();
        product1.setId(1);
        product1.setPrice(String.valueOf(1000));
        product1.setName("Product Test 1");
        product1.setDescription("Product Test Description 1");
        ProductResponse product2 = new ProductResponse();
        product2.setId(2);
        product2.setPrice(String.valueOf(2000));
        product2.setName("Product Test 2");
        product2.setDescription("Product Test Description 2");
        ApiResponse<List<ProductResponse>> productApiResponse = ApiResponse.<List<ProductResponse>>builder()
                .result(List.of(product1, product2))
                .build();

        UserResponse user = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test Name")
                .build();

        when(userClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headerSpec);
        when(headerSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(headerSpec);
        when(headerSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserResponse.class)).thenReturn(Mono.just(user));

        when(productClient.getProductsByListId(any())).thenReturn(productApiResponse);
        when(inventoryClient.isInStock(any(), any())).thenReturn(new InventoryResponse(false));

        assertThatThrownBy(() -> orderService.createOrder(orderRequest)).hasMessageContaining(ErrorCode.PRODUCT_OUT_OF_STOCK.getMessage());
    }

    @Test
    void getReport() {
        String startDate = LocalDateTime.now().minusMonths(1).toString();
        String endDate = LocalDateTime.now().plusMonths(1).toString();

        ProductResponse expectedProduct1 = new ProductResponse();
        expectedProduct1.setId(1);
        expectedProduct1.setPrice(String.valueOf(1000));
        expectedProduct1.setName("Product Test 1");
        expectedProduct1.setDescription("Product Test Description 1");
        ProductResponse expectedProduct2 = new ProductResponse();
        expectedProduct2.setId(2);
        expectedProduct2.setPrice(String.valueOf(2000));
        expectedProduct2.setName("Product Test 2");
        expectedProduct2.setDescription("Product Test Description 2");
        ApiResponse<List<ProductResponse>> expectedProductApiResponse = ApiResponse.<List<ProductResponse>>builder()
                .result(List.of(expectedProduct1, expectedProduct2))
                .build();

        ReportResponse expectedReportResponse = ReportResponse.builder()
                .reportPeriod("61 day(s)")
                .avgOrderValue("3000.00")
                .totalOrders(1L)
                .newCustomers(1L)
                .returningCustomers(0L)
                .pending(1L)
                .processing(0L)
                .shipping(0L)
                .delivered(0L)
                .canceled(0L)
                .topSellingProducts(expectedProductApiResponse.getResult())
                .build();

        when(productClient.getProductsByListId(any())).thenReturn(expectedProductApiResponse);

        ReportResponse reportResponse = orderService.getReport(startDate, endDate);

        assertThat(reportResponse)
                .as("Check report response is not null")
                .isNotNull();
        assertThat(reportResponse.getReportPeriod())
                .as("Check report period")
                .isEqualTo(expectedReportResponse.getReportPeriod());
        assertThat(reportResponse.getAvgOrderValue())
                .as("Check average order value")
                .isEqualTo(expectedReportResponse.getAvgOrderValue());
        assertThat(reportResponse.getTotalOrders())
                .as("Check total orders")
                .isEqualTo(expectedReportResponse.getTotalOrders());
        assertThat(reportResponse.getNewCustomers())
                .as("Check new customers")
                .isEqualTo(expectedReportResponse.getNewCustomers());
        assertThat(reportResponse.getReturningCustomers())
                .as("Check returning customers")
                .isEqualTo(expectedReportResponse.getReturningCustomers());
        assertThat(reportResponse.getPending())
                .as("Check pending orders")
                .isEqualTo(expectedReportResponse.getPending());
        assertThat(reportResponse.getProcessing())
                .as("Check processing orders")
                .isEqualTo(expectedReportResponse.getProcessing());
        assertThat(reportResponse.getShipping())
                .as("Check shipping orders")
                .isEqualTo(expectedReportResponse.getShipping());
        assertThat(reportResponse.getDelivered())
                .as("Check delivered orders")
                .isEqualTo(expectedReportResponse.getDelivered());
        assertThat(reportResponse.getCanceled())
                .as("Check canceled orders")
                .isEqualTo(expectedReportResponse.getCanceled());
        assertThat(reportResponse.getTopSellingProducts().size())
                .as("Check top selling products size")
                .isEqualTo(2);

        ProductResponse productResponse1 = reportResponse.getTopSellingProducts().getFirst();
        ProductResponse productResponse2 = reportResponse.getTopSellingProducts().getLast();
        assertThat(productResponse1.getId())
                .as("Check product 1 id")
                .isEqualTo(expectedProduct2.getId());
        assertThat(productResponse1.getName())
                .as("Check product 1 name")
                .isEqualTo(expectedProduct2.getName());
        assertThat(productResponse1.getDescription())
                .as("Check product 1 description")
                .isEqualTo(expectedProduct2.getDescription());
        assertThat(productResponse1.getPrice())
                .as("Check product 1 price")
                .isEqualTo(expectedProduct2.getPrice());
        assertThat(productResponse2.getId())
                .as("Check product 2 id")
                .isEqualTo(expectedProduct1.getId());
        assertThat(productResponse2.getName())
                .as("Check product 2 name")
                .isEqualTo(expectedProduct1.getName());
        assertThat(productResponse2.getDescription())
                .as("Check product 2 description")
                .isEqualTo(expectedProduct1.getDescription());
        assertThat(productResponse2.getPrice())
                .as("Check product 2 price")
                .isEqualTo(expectedProduct1.getPrice());
    }
}
