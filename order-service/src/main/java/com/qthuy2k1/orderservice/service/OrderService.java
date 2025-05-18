package com.qthuy2k1.orderservice.service;

import com.qthuy2k1.orderservice.dto.request.*;
import com.qthuy2k1.orderservice.dto.response.*;
import com.qthuy2k1.orderservice.enums.ErrorCode;
import com.qthuy2k1.orderservice.enums.OrderStatus;
import com.qthuy2k1.orderservice.event.OrderItemPlaced;
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
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService implements IOrderService {
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    OrderReportRepository orderReportRepository;
    KafkaTemplate<String, OrderPlaced> orderPlacedKafkaTemplate;
    KafkaTemplate<String, List<ReduceInventoryRequest>> reduceInventoryKafkaTemplate;
    @LoadBalanced
    WebClient.Builder webClientBuilder;
    ProductClient productClient;
    InventoryClient inventoryClient;
    OrderMapper orderMapper;
    OrderItemMapper orderItemMapper;
    String PRICE_FORMAT = "#.00";
    String PERIOD_DAYS = " day(s)";
    WebClient userClient;

    @Transactional
    @Retry(name = "createOrder")
    @CircuitBreaker(name = "createOrder", fallbackMethod = "createOrderFallback")
    public OrderResponse createOrder(OrderRequest orderRequest) throws ExecutionException, InterruptedException {
        String statusLabel = orderRequest.getStatus();
        if (statusLabel == null) {
            orderRequest.setStatus(OrderStatus.PENDING.getLabel());
        }

        Set<OrderItemRequest> orderItemsRequest = orderRequest.getOrderItem();

        // list of product ids, separated by commas (e.g. 1,2,3)
        String ids = orderItemsRequest.stream()
                .map(orderItem -> String.valueOf(orderItem.getProductId()))
                .collect(Collectors.joining(","));

        // for authentication(jwt token) in asynchronous calls
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // asynchronous calls
        CompletableFuture<Map<Integer, ProductResponse>> productFuture = fetchProductMapAsync(requestAttributes, ids);
        CompletableFuture<UserResponse> userFuture = fetchUserAsync(requestAttributes);

        return productFuture.thenCombine(userFuture, (products, user) -> {
            // for reducing product stocks in inventory service
            List<ReduceInventoryRequest> reduceInventoryRequests = new ArrayList<>();
            Set<OrderItemModel> orderItemModels = mapToOrderItems(orderItemsRequest, products, reduceInventoryRequests);
            OrderModel orderModel = buildOrder(orderRequest, user, orderItemModels);

            // store to db and get the order back
            orderItemModels.forEach(orderItem -> orderItem.setOrder(orderModel));
            OrderModel orderSaved = orderRepository.save(orderModel);

            Set<OrderItemPlaced> orderItemPlaceds = orderSaved.getOrderItems()
                    .stream()
                    .map(orderItem -> OrderItemPlaced.builder()
                            .productName(products.get(orderItem.getProductId()).getName())
                            .price(orderItem.getPrice())
                            .quantity(orderItem.getQuantity())
                            .build())
                    .collect(Collectors.toSet());

            sendOrderNotification(orderSaved, orderItemPlaceds, reduceInventoryRequests);

            return orderMapper.toOrderResponse(orderSaved);
        }).get();
    }

    public OrderResponse createOrderFallback(OrderRequest orderRequest, Throwable throwable) {
        log.error("Fallback triggered for createOrder due to: {}", throwable.getMessage(), throwable);
        Throwable actualThrowable = throwable instanceof ExecutionException ? throwable.getCause() : throwable;
        if (actualThrowable instanceof AppException) {
            // If it's an AppException, rethrow it
            throw (AppException) actualThrowable;
        } else {
            // For other exceptions, return the fallback
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    private OrderModel buildOrder(OrderRequest orderRequest,
                                  UserResponse user,
                                  Set<OrderItemModel> orderItemModels) {
        orderRequest.getOrderItem().forEach(orderItem -> {
            // for requesting to inventory service to check whether product is in stock or not
            InventoryResponse inventoryResponses = inventoryClient.isInStock(orderItem.getQuantity(), orderItem.getProductId());
            if (inventoryResponses == null) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            if (!inventoryResponses.isInStock()) {
                throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
            }
        });

        OrderModel orderModel = orderMapper.toOder(orderRequest);

        // calculate and update total amount
        BigDecimal totalAmount = orderItemModels.stream()
                .map(orderItem -> orderItem
                        .getPrice()
                        .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orderModel.setTotalAmount(totalAmount);
        orderModel.setUserId(user.getId());
        orderModel.setOrderItems(orderItemModels);

        return orderModel;
    }

    private Set<OrderItemModel> mapToOrderItems(
            Set<OrderItemRequest> orderItemRequests,
            Map<Integer, ProductResponse> products,
            List<ReduceInventoryRequest> reduceInventoryRequests) {
        Set<OrderItemModel> orderItemModels = new HashSet<>();
        orderItemRequests.forEach(orderItem -> {
            ProductResponse product = products.get(orderItem.getProductId());
            orderItemModels.add(OrderItemModel.builder()
                    .productId(product.getId())
                    .price(new BigDecimal(product.getPrice()))
                    .quantity(orderItem.getQuantity())
                    .build()
            );
            reduceInventoryRequests.add(ReduceInventoryRequest.builder()
                    .productId(product.getId())
                    .reduceBy(orderItem.getQuantity())
                    .build());
        });

        return orderItemModels;
    }

    private CompletableFuture<Map<Integer, ProductResponse>> fetchProductMapAsync(ServletRequestAttributes requestAttributes, String ids) {
        return CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            ApiResponse<List<ProductResponse>> productList = productClient.getProductsByListId(ids);
            if (productList.getResult() == null) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            return productList
                    .getResult()
                    .stream()
                    .collect(Collectors.toMap(ProductResponse::getId, product -> product));
        });
    }

    private CompletableFuture<UserResponse> fetchUserAsync(ServletRequestAttributes requestAttributes) {
        return CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            return Optional.ofNullable(getUserInfo(requestAttributes))
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        });
    }

    private void sendOrderNotification(
            OrderModel orderSaved,
            Set<OrderItemPlaced> orderItemPlaceds,
            List<ReduceInventoryRequest> reduceInventoryRequests) {
        // send notification
        OrderPlaced orderPlaced = OrderPlaced.builder()
                .status(orderSaved.getStatus().getLabel())
                .totalAmount(String.valueOf(orderSaved.getTotalAmount()))
                .createdAt(LocalDateTime.now().toString())
                .createdAt(LocalDateTime.now().toString())
                .orderItems(orderItemPlaceds)
                .build();

        // send a message to create-order topic
        orderPlacedKafkaTemplate.send("create-order", orderPlaced);

        // send a message to reduce-product-stock
        reduceInventoryKafkaTemplate.send("reduce-product-stock", reduceInventoryRequests);
    }

    public List<OrderGraphQLResponse> getAllOrdersGraphQL() {
        List<OrderGraphQLResponse> orderGraphQLResponsesList = new ArrayList<>();
        List<OrderModel> orderModelList = orderRepository.findAll();

        // Product GraphQL Request
        String productGraphQLQuery = """
                query Product($ids: [Int!]!) {
                    getProductGraphQLByListId(ids: $ids) {
                        id
                        name
                        description
                        price
                        skuCode
                        category {
                            id
                            name
                            description
                        }
                    }
                }
                """;

        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (servletRequestAttributes == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");

        if (!StringUtils.hasText(authHeader)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String productGraphQLOperationName = "Product";
        String productGraphQLVariableName = "ids";
        String productURL = "http://product-service/products/graphql"; // graphQL url
        HttpGraphQlClient graphQlClient = HttpGraphQlClient
                .builder(webClientBuilder
                        .baseUrl(productURL)
                        .defaultHeader("Authorization", authHeader)
                        .build())
                .build();

        for (OrderModel orderModel : orderModelList) {
            List<OrderItemGraphQLResponse> orderItemGraphQLResponsesList = new ArrayList<>();

            List<OrderItemModel> orderItemModelList =
                    orderItemRepository.findAllByOrderId(orderModel.getId());
            List<Integer> productIdList = orderItemModelList.stream().map(OrderItemModel::getProductId).toList();

            // Request to product service using graphQL query to retrieve product response
            List<ProductResponse> productList = graphQlClient
                    .document(productGraphQLQuery)
                    .operationName(productGraphQLOperationName)
                    .variable(productGraphQLVariableName, productIdList)
                    .retrieve("getProductGraphQLByListId") // which is the product response
                    .toEntityList(ProductResponse.class).block();

            // throw exception if product not found
            if (CollectionUtils.isEmpty(productList)) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            for (OrderItemModel orderItem : orderItemModelList) {
                OrderItemGraphQLResponse orderItemGraphQLResponse = orderItemMapper.toOrderItemGraphQLResponse(orderItem);
                orderItemGraphQLResponse.setProduct(
                        productList
                                .stream()
                                .filter(
                                        product -> product.getId().equals(orderItem.getProductId()))
                                .findFirst()
                                .orElse(null)
                );
                orderItemGraphQLResponsesList.add(orderItemGraphQLResponse);
            }

            // request to user service to retrieve the user response
            ServletRequestAttributes requestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
            UserResponse user = Optional.ofNullable(getUserInfo(servletRequestAttributes))
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            // convert order item list to order item set
            OrderGraphQLResponse orderGraphQLResponse = orderMapper.toOrderGraphQLResponse(orderModel);
            orderGraphQLResponse.setUser(user);
            orderGraphQLResponse.setOrderItems(orderItemGraphQLResponsesList);
            orderGraphQLResponsesList.add(orderGraphQLResponse);
        }

        return orderGraphQLResponsesList;
    }

    public ReportResponse getReport(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            LocalDateTime parsedStart = LocalDateTime.parse(startDate, formatter);
            // format it again just in case
            startDate = parsedStart.format(formatter);
        } catch (DateTimeParseException e) {
            // default ISO_LOCAL_DATE_TIME
            LocalDateTime parsedStart = LocalDateTime.parse(startDate);
            startDate = parsedStart.format(formatter);
        }

        try {
            LocalDateTime parsedEnd = LocalDateTime.parse(endDate, formatter);
            endDate = parsedEnd.format(formatter);
        } catch (DateTimeParseException e) {
            LocalDateTime parsedEnd = LocalDateTime.parse(endDate);
            endDate = parsedEnd.format(formatter);
        }

        ReportModel report = orderReportRepository.getOrderReport(startDate, endDate);
        ReportResponse reportResponse = toReportResponse(report);
        // get the product response list
        List<ProductReportList> productReportList = orderReportRepository.getProductReportList();
        String productIds = productReportList.stream()
                .map(productReport -> String.valueOf(productReport.getProduct_id()))
                .collect(Collectors.joining(","));
        ApiResponse<List<ProductResponse>> productListResp = productClient.getProductsByListId(productIds);
        if (productListResp.getResult() == null) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        // map product list to a hashmap(productid, product)
        Map<Integer, ProductResponse> productList = productListResp
                .getResult()
                .stream()
                .collect(Collectors.toMap(ProductResponse::getId, product -> product));

        List<ProductResponse> productReportListResponse = new ArrayList<>();
        productReportList.forEach(productReport -> {
            productReportListResponse.add(productList.get(productReport.getProduct_id()));
        });

        reportResponse.setTopSellingProducts(productReportListResponse);
        return reportResponse;
    }

    public void updateStatusOrder(Integer id, UpdateStatusOrderRequest orderRequest) {
        OrderModel order = orderRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        order.setStatus(OrderStatus.fromLabel(orderRequest.getStatus()));
        orderRepository.save(order);
    }

    @KafkaListener(topics = "update-order", groupId = "update-order-group")
    public void handleKafkaPaidOrderUpdate(Integer id, UpdatePaidOrderRequest orderRequest) {
        log.info("Updating paid order with ID: {}", id);
        OrderModel order = orderRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        order.setPaid(true);
        orderRepository.save(order);
    }

    private ReportResponse toReportResponse(ReportModel report) {
        return ReportResponse.builder()
                .reportPeriod(report.getPeriod() + PERIOD_DAYS)
                .totalOrders(report.getTotalorders())
                .avgOrderValue(new DecimalFormat(PRICE_FORMAT).format(report.getAvgordervalue()))
                .newCustomers(report.getNewcustomers())
                .returningCustomers(report.getReturningcustomers())
                .pending(report.getPending())
                .shipping(report.getShipped())
                .processing(report.getProcessing())
                .delivered(report.getDelivered())
                .canceled(report.getCanceled())
                .build();
    }

    public UserResponse getUserInfo(ServletRequestAttributes requestAttributes) {
        String authHeader = requestAttributes.getRequest().getHeader("Authorization");
        if (!StringUtils.hasText(authHeader)) {
            return null;
        }
        return userClient.get()
                .uri("/realms/my-realm/protocol/openid-connect/userinfo")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .block();
    }
}
