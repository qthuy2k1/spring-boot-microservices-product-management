package com.qthuy2k1.orderservice.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.qthuy2k1.orderservice.OrderApplication;
import com.qthuy2k1.orderservice.dto.request.OrderItemRequest;
import com.qthuy2k1.orderservice.dto.request.OrderRequest;
import com.qthuy2k1.orderservice.dto.response.*;
import com.qthuy2k1.orderservice.enums.ErrorCode;
import com.qthuy2k1.orderservice.repository.feign.InventoryClient;
import com.qthuy2k1.orderservice.repository.feign.ProductClient;
import com.qthuy2k1.orderservice.repository.feign.UserClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = OrderApplication.class,
        properties = "spring.profiles.active=test"
)
@ExtendWith(SpringExtension.class)
@DirtiesContext
@WithMockUser(username = "test", roles = "ADMIN")
public class OrderControllerTest extends BaseControllerTest {
    @MockBean
    InventoryClient inventoryClient;
    @MockBean
    ProductClient productClient;
    @MockBean
    UserClient userClient;

    @Test
    void createOrder() throws Exception {
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

        OrderResponse expectedOrderResponse = OrderResponse.builder()
                .status("PENDING")
                .totalAmount(BigDecimal.valueOf(5000))
                .userId(1)
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
                .id(1)
                .email("test@example.com")
                .name("Test Name")
                .build();
        ApiResponse<UserResponse> userApiResponse = new ApiResponse<>();
        userApiResponse.setResult(user);

        when(productClient.getProductsByListId(any())).thenReturn(productApiResponse);
        when(userClient.getUserByEmail(anyString())).thenReturn(userApiResponse);
        when(inventoryClient.isInStock(any(), any())).thenReturn(new InventoryResponse(true));

        MvcResult createOrderResult = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ApiResponse<OrderResponse> createOrderApiResponse = objectMapper.readValue(
                createOrderResult.getResponse().getContentAsByteArray(),
                new TypeReference<ApiResponse<OrderResponse>>() {
                }
        );

        assertThat(createOrderApiResponse)
                .as("Check that the createOrderApiResponse is not null")
                .isNotNull();
        assertThat(createOrderApiResponse.getCode())
                .as("Check that the response code matches the default code")
                .isEqualTo(DEFAULT_CODE_RESPONSE);
        assertThat(createOrderApiResponse.getResult())
                .as("Check that the result object is not null")
                .isNotNull();
        assertThat(createOrderApiResponse.getResult().getStatus())
                .as("Check that the order status matches the expected status")
                .isEqualTo(expectedOrderResponse.getStatus());
        assertThat(createOrderApiResponse.getResult().getUserId())
                .as("Check that the user ID in the response matches the expected user ID")
                .isEqualTo(expectedOrderResponse.getUserId());
        assertThat(createOrderApiResponse.getResult().getTotalAmount())
                .as("Check that the total amount in the response matches the expected total amount")
                .isEqualTo(expectedOrderResponse.getTotalAmount());

        List<OrderItemResponse> createdOrderItemList = createOrderApiResponse.getResult().getOrderItems()
                .stream().sorted(Comparator.comparing(OrderItemResponse::getProductId)).toList();

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
    void createOrder_ServiceUnavailable() throws Exception {
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
        ApiResponse<OrderResponse> orderApiResponse = ApiResponse.<OrderResponse>builder()
                .code(ErrorCode.SERVICE_UNAVAILABLE.getCode())
                .message(ErrorCode.SERVICE_UNAVAILABLE.getMessage())
                .build();

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(content().string(objectMapper.writeValueAsString(orderApiResponse)));
    }

    @Test
    void createOrder_ProductNotFound() throws Exception {
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
                .id(1)
                .email("test@example.com")
                .name("Test Name")
                .build();
        ApiResponse<UserResponse> userApiResponse = new ApiResponse<>();
        userApiResponse.setResult(user);

        when(productClient.getProductsByListId(any())).thenReturn(productApiResponse);
        when(userClient.getUserByEmail(anyString())).thenReturn(userApiResponse);
        when(inventoryClient.isInStock(any(), any())).thenReturn(new InventoryResponse(true));

        ApiResponse<OrderResponse> orderApiResponse = ApiResponse.<OrderResponse>builder()
                .code(ErrorCode.PRODUCT_NOT_FOUND.getCode())
                .message(ErrorCode.PRODUCT_NOT_FOUND.getMessage())
                .build();

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(objectMapper.writeValueAsString(orderApiResponse)));
    }

    @Test
    void createOrder_UserNotFound() throws Exception {
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

        ApiResponse<UserResponse> userApiResponse = new ApiResponse<>();
        userApiResponse.setResult(null);

        when(productClient.getProductsByListId(any())).thenReturn(productApiResponse);
        when(userClient.getUserByEmail(anyString())).thenReturn(userApiResponse);
        when(inventoryClient.isInStock(any(), any())).thenReturn(new InventoryResponse(true));

        ApiResponse<OrderResponse> orderApiResponse = ApiResponse.<OrderResponse>builder()
                .code(ErrorCode.USER_NOT_FOUND.getCode())
                .message(ErrorCode.USER_NOT_FOUND.getMessage())
                .build();

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(objectMapper.writeValueAsString(orderApiResponse)));
    }

    @Test
    void createOrder_ProductOutOfStock() throws Exception {
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
                .id(1)
                .email("test@example.com")
                .name("Test Name")
                .build();
        ApiResponse<UserResponse> userApiResponse = new ApiResponse<>();
        userApiResponse.setResult(user);

        when(productClient.getProductsByListId(any())).thenReturn(productApiResponse);
        when(userClient.getUserByEmail(anyString())).thenReturn(userApiResponse);
        when(inventoryClient.isInStock(any(), any())).thenReturn(new InventoryResponse(false));

        ApiResponse<OrderResponse> orderApiResponse = ApiResponse.<OrderResponse>builder()
                .code(ErrorCode.PRODUCT_OUT_OF_STOCK.getCode())
                .message(ErrorCode.PRODUCT_OUT_OF_STOCK.getMessage())
                .build();

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(objectMapper.writeValueAsString(orderApiResponse)));
    }

    @Test
    void getReport() throws Exception {
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

        MvcResult createOrderResult = mockMvc.perform(get("/orders/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<ReportResponse> createOrderApiResponse = objectMapper.readValue(
                createOrderResult.getResponse().getContentAsByteArray(),
                new TypeReference<ApiResponse<ReportResponse>>() {
                }
        );

        assertThat(createOrderApiResponse)
                .as("Check report response is not null")
                .isNotNull();
        assertThat(createOrderApiResponse.getResult().getReportPeriod())
                .as("Check report period")
                .isEqualTo(expectedReportResponse.getReportPeriod());
        assertThat(createOrderApiResponse.getResult().getAvgOrderValue())
                .as("Check average order value")
                .isEqualTo(expectedReportResponse.getAvgOrderValue());
        assertThat(createOrderApiResponse.getResult().getTotalOrders())
                .as("Check total orders")
                .isEqualTo(expectedReportResponse.getTotalOrders());
        assertThat(createOrderApiResponse.getResult().getNewCustomers())
                .as("Check new customers")
                .isEqualTo(expectedReportResponse.getNewCustomers());
        assertThat(createOrderApiResponse.getResult().getReturningCustomers())
                .as("Check returning customers")
                .isEqualTo(expectedReportResponse.getReturningCustomers());
        assertThat(createOrderApiResponse.getResult().getPending())
                .as("Check pending orders")
                .isEqualTo(expectedReportResponse.getPending());
        assertThat(createOrderApiResponse.getResult().getProcessing())
                .as("Check processing orders")
                .isEqualTo(expectedReportResponse.getProcessing());
        assertThat(createOrderApiResponse.getResult().getShipping())
                .as("Check shipping orders")
                .isEqualTo(expectedReportResponse.getShipping());
        assertThat(createOrderApiResponse.getResult().getDelivered())
                .as("Check delivered orders")
                .isEqualTo(expectedReportResponse.getDelivered());
        assertThat(createOrderApiResponse.getResult().getCanceled())
                .as("Check canceled orders")
                .isEqualTo(expectedReportResponse.getCanceled());
        assertThat(createOrderApiResponse.getResult().getTopSellingProducts().size())
                .as("Check top selling products size")
                .isEqualTo(2);

        ProductResponse productResponse1 = createOrderApiResponse.getResult().getTopSellingProducts().getFirst();
        ProductResponse productResponse2 = createOrderApiResponse.getResult().getTopSellingProducts().getLast();
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
