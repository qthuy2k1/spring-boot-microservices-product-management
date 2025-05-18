package com.qthuy2k1.orderservice.integration.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.qthuy2k1.orderservice.OrderApplication;
import com.qthuy2k1.orderservice.dto.request.OrderItemRequest;
import com.qthuy2k1.orderservice.dto.request.OrderRequest;
import com.qthuy2k1.orderservice.dto.request.UpdateStatusOrderRequest;
import com.qthuy2k1.orderservice.dto.response.*;
import com.qthuy2k1.orderservice.enums.ErrorCode;
import com.qthuy2k1.orderservice.enums.OrderStatus;
import com.qthuy2k1.orderservice.repository.feign.InventoryClient;
import com.qthuy2k1.orderservice.repository.feign.ProductClient;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = OrderApplication.class,
        properties = "spring.profiles.active=test"
)
@ExtendWith(SpringExtension.class)
@DirtiesContext
public class OrderControllerTest extends BaseControllerTest {
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
    void createOrder() throws Exception {
        String token = getAdminToken();
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
                .userId(userId.toString())
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

        String createOrderResponseBody = given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(orderRequest))
                .when().post("/orders")
                .then().statusCode(HttpStatus.CREATED.value())
                .extract().asString();

        ApiResponse<OrderResponse> createOrderApiResponse = objectMapper.readValue(
                createOrderResponseBody,
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
        String token = getAdminToken();
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

        given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(orderRequest))
                .when().post("/orders")
                .then().statusCode(ErrorCode.SERVICE_UNAVAILABLE.getStatusCode().value())
                .body(equalTo(objectMapper.writeValueAsString(orderApiResponse)));

    }

    @Test
    void createOrder_ProductNotFound() throws Exception {
        String token = getAdminToken();
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

        when(userClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headerSpec);
        when(headerSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(headerSpec);
        when(headerSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserResponse.class)).thenReturn(Mono.just(user));

        when(productClient.getProductsByListId(any())).thenReturn(productApiResponse);
        when(inventoryClient.isInStock(any(), any())).thenReturn(new InventoryResponse(true));

        ApiResponse<OrderResponse> orderApiResponse = ApiResponse.<OrderResponse>builder()
                .code(ErrorCode.PRODUCT_NOT_FOUND.getCode())
                .message(ErrorCode.PRODUCT_NOT_FOUND.getMessage())
                .build();

        given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(orderRequest))
                .when().post("/orders")
                .then().statusCode(ErrorCode.PRODUCT_NOT_FOUND.getStatusCode().value())
                .body(equalTo(objectMapper.writeValueAsString(orderApiResponse)));
    }

    @Test
    void createOrder_UserNotFound() throws Exception {
        String token = getAdminToken();
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

        ApiResponse<OrderResponse> orderApiResponse = ApiResponse.<OrderResponse>builder()
                .code(ErrorCode.USER_NOT_FOUND.getCode())
                .message(ErrorCode.USER_NOT_FOUND.getMessage())
                .build();
        given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(orderRequest))
                .when().post("/orders")
                .then().statusCode(ErrorCode.USER_NOT_FOUND.getStatusCode().value())
                .body(equalTo(objectMapper.writeValueAsString(orderApiResponse)));
    }

    @Test
    void createOrder_ProductOutOfStock() throws Exception {
        String token = getAdminToken();
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

        ApiResponse<OrderResponse> orderApiResponse = ApiResponse.<OrderResponse>builder()
                .code(ErrorCode.PRODUCT_OUT_OF_STOCK.getCode())
                .message(ErrorCode.PRODUCT_OUT_OF_STOCK.getMessage())
                .build();
        given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(orderRequest))
                .when().post("/orders")
                .then().statusCode(ErrorCode.PRODUCT_OUT_OF_STOCK.getStatusCode().value())
                .body(equalTo(objectMapper.writeValueAsString(orderApiResponse)));
    }

    @Test
    void getReport() throws Exception {
        String token = getAdminToken();
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

        String getReportResponseBody = given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .queryParam("startDate", startDate)
                .queryParam("endDate", endDate)
                .when().get("/orders/report")
                .then().statusCode(HttpStatus.OK.value())
                .extract().asString();

        ApiResponse<ReportResponse> createOrderApiResponse = objectMapper.readValue(
                getReportResponseBody,
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

    @Test
    void updateOrder() throws JsonProcessingException, URISyntaxException {
        String token = getAdminToken();
        int id = savedOrder.getId();
        UpdateStatusOrderRequest orderRequest = UpdateStatusOrderRequest.builder()
                .status(OrderStatus.DELIVERED.getLabel())
                .build();

        given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(orderRequest))
                .when().put("/orders/" + id)
                .then().statusCode(HttpStatus.OK.value())
                .body(equalTo(MessageResponse.SUCCESS));
    }

    @Test
    void updateOrder_OrderNotFound() throws JsonProcessingException, URISyntaxException {
        String token = getAdminToken();
        int id = savedOrder.getId() + 1;
        OrderRequest orderRequest = OrderRequest.builder()
                .status(OrderStatus.DELIVERED.getLabel())
                .build();

        ApiResponse<OrderResponse> orderApiResponse = ApiResponse.<OrderResponse>builder()
                .code(ErrorCode.ORDER_NOT_FOUND.getCode())
                .message(ErrorCode.ORDER_NOT_FOUND.getMessage())
                .build();
        given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(orderRequest))
                .when().put("/orders/" + id)
                .then().statusCode(ErrorCode.ORDER_NOT_FOUND.getStatusCode().value())
                .body(equalTo(objectMapper.writeValueAsString(orderApiResponse)));
    }
}
