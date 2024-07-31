package com.qthuy2k1.orderservice.service;

import com.qthuy2k1.orderservice.dto.request.OrderItemRequest;
import com.qthuy2k1.orderservice.dto.request.OrderRequest;
import com.qthuy2k1.orderservice.dto.response.*;
import com.qthuy2k1.orderservice.enums.ErrorCode;
import com.qthuy2k1.orderservice.event.OrderItemPlaced;
import com.qthuy2k1.orderservice.event.OrderPlaced;
import com.qthuy2k1.orderservice.exception.AppException;
import com.qthuy2k1.orderservice.mapper.OrderItemMapper;
import com.qthuy2k1.orderservice.mapper.OrderMapper;
import com.qthuy2k1.orderservice.model.OrderItemModel;
import com.qthuy2k1.orderservice.model.OrderModel;
import com.qthuy2k1.orderservice.repository.OrderItemRepository;
import com.qthuy2k1.orderservice.repository.OrderRepository;
import com.qthuy2k1.orderservice.repository.feign.InventoryClient;
import com.qthuy2k1.orderservice.repository.feign.ProductClient;
import com.qthuy2k1.orderservice.repository.feign.UserClient;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    KafkaTemplate<String, OrderPlaced> kafkaTemplate;
    @LoadBalanced
    WebClient.Builder webClientBuilder;
    UserClient userClient;
    ProductClient productClient;
    InventoryClient inventoryClient;
    OrderMapper orderMapper;
    OrderItemMapper orderItemMapper;

    public OrderResponse createOrder(OrderRequest orderRequest) {
        orderRequest.setStatus("PENDING");

        Set<OrderItemRequest> orderItemsRequest = orderRequest.getOrderItem();
        Set<OrderItemPlaced> orderItemPlacedSet = new HashSet<>();

        orderItemsRequest.forEach(orderItem -> {
            // request to product service to retrieve the product name
            ApiResponse<ProductResponse> product = productClient.getProduct(orderItem.getProductId()).getBody();

            // throw exception if product not found
            if (product == null) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            } else {
                if (product.getResult() == null || product.getResult().getId() == null) {
                    if (product.getMessage().equals(ErrorCode.PRODUCT_NOT_FOUND.getMessage())) {
                        throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
                    }
                }
            }

            // update the product price to order item request
            orderItem.setPrice(new BigDecimal(product.getResult().getPrice()));
            OrderItemPlaced orderItemPlaced = new OrderItemPlaced();
            orderItemPlaced.setProductName(product.getResult().getName());
            orderItemPlaced.setPrice(new BigDecimal(product.getResult().getPrice()));
            orderItemPlaced.setQuantity(orderItem.getQuantity());

            // add to orderItemPlaced Set
            orderItemPlacedSet.add(orderItemPlaced);
        });

        OrderModel orderModel = orderMapper.toOder(orderRequest);

        // calculate total amount
        BigDecimal totalAmount = orderRequest.getOrderItem().stream()
                .map(orderItem ->
                        orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // update total amount
        orderModel.setTotalAmount(totalAmount);

        // Check user exists
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email == null || email.isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        ApiResponse<UserResponse> user = userClient.getUserByEmail(email);
        // throw exception if user not found
        if (user == null || user.getResult() == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        // set user id
        orderModel.setUserId(user.getResult().getId());

        // store to db and get the order back
        OrderModel orderSaved = orderRepository.save(orderModel);

        HashSet<OrderItemModel> orderItemModelsSaved = new HashSet<>();
        // store all order item to db
        orderRequest.getOrderItem().forEach(orderItem -> {
            // for requesting to inventory service to check whether product is in stock or not
            InventoryResponse inventoryResponses = inventoryClient.isInStock(orderItem.getQuantity(), orderItem.getProductId());
            if (inventoryResponses == null) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            if (!inventoryResponses.isInStock()) {
                throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
            }

            // store order item
            orderItem.setOrder(orderSaved);
            orderItemModelsSaved.add(orderItemRepository.save(orderItemMapper.toOrderItem(orderItem)));
        });

        // send notification
        OrderPlaced orderPlaced = new OrderPlaced();
        orderPlaced.setStatus(orderSaved.getStatus());
        orderPlaced.setTotalAmount(String.valueOf(orderSaved.getTotalAmount()));
        orderPlaced.setCreatedAt(LocalDateTime.now().toString());
        orderPlaced.setUpdatedAt(LocalDateTime.now().toString());
        orderPlaced.setOrderItems(orderItemPlacedSet);

        // produce message to create-order topic
        kafkaTemplate.send("create-order", orderPlaced);

        orderSaved.setOrderItems(orderItemModelsSaved);

        return orderMapper.toOrderResponse(orderSaved);
    }

    public List<OrderGraphQLResponse> getAllOrdersGraphQL() {
        List<OrderGraphQLResponse> orderGraphQLResponsesList = new ArrayList<>();
        List<OrderModel> orderModelList = orderRepository.findAll();
        log.info(orderModelList.toString());

        // Product GraphQL Request
        String productGraphQLQuery = """
                query Product($id: Int!){
                    productById(id: $id) {
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
        String productGraphQLOperationName = "Product";
        String productGraphQLVariableName = "id";
        String productURL = "http://product-service/products/graphql"; // graphQL url
        HttpGraphQlClient graphQlClient = HttpGraphQlClient
                .builder(webClientBuilder.baseUrl(productURL).build())
                .build();

        for (OrderModel orderModel : orderModelList) {
            List<OrderItemGraphQLResponse> orderItemGraphQLResponsesList = new ArrayList<>();

            List<OrderItemModel> orderItemModelList =
                    orderItemRepository.findAllByOrderId(orderModel.getId());
            for (OrderItemModel orderItem : orderItemModelList) {

                // Request to product service using graphQL query to retrieve product response
                ProductResponse product = graphQlClient
                        .document(productGraphQLQuery)
                        .operationName(productGraphQLOperationName)
                        .variable(productGraphQLVariableName, orderItem.getProductId())
                        .retrieve("productById") // which is the product response
                        .toEntity(ProductResponse.class).block();

                // throw exception if product not found
                if (product == null) {
                    throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
                }

                OrderItemGraphQLResponse orderItemGraphQLResponse = orderItemMapper.toOrderItemGraphQLResponse(orderItem);
                orderItemGraphQLResponse.setProduct(product);
                orderItemGraphQLResponsesList.add(orderItemGraphQLResponse);
            }

            // request to user service to retrieve the user response
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            if (email == null || email.isEmpty()) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
            ApiResponse<UserResponse> user = userClient.getUserByEmail(email);

            // throw exception if user not found
            if (user == null || user.getResult() == null) {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }

            // convert order item list to order item set
            OrderGraphQLResponse orderGraphQLResponse = orderMapper.toOrderGraphQLResponse(orderModel);
            orderGraphQLResponse.setUser(user.getResult());
            orderGraphQLResponse.setOrderItems(orderItemGraphQLResponsesList);
            orderGraphQLResponsesList.add(orderGraphQLResponse);
        }

        return orderGraphQLResponsesList;
    }

    public void updateOrder(Integer id, OrderRequest orderRequest) {
        OrderModel order = orderRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        order.setStatus(orderRequest.getStatus());
        orderRepository.save(order);
    }
}
