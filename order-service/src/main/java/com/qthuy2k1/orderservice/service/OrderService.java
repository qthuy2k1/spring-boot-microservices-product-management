package com.qthuy2k1.orderservice.service;

import com.qthuy2k1.orderservice.dto.*;
import com.qthuy2k1.orderservice.event.OrderItemPlaced;
import com.qthuy2k1.orderservice.event.OrderPlaced;
import com.qthuy2k1.orderservice.exception.NotFoundEnumException;
import com.qthuy2k1.orderservice.exception.NotFoundException;
import com.qthuy2k1.orderservice.model.OrderModel;
import com.qthuy2k1.orderservice.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.kafka.core.KafkaTemplate;
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
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final KafkaTemplate<String, OrderPlaced> kafkaTemplate;
    @LoadBalanced
    private final WebClient.Builder webClientBuilder;

    public void createOrder(OrderRequest orderRequest) throws NotFoundException {
        // Check user exists
        String userUri = String.format("http://%s/api/v1/%s/%s/is-exists",
                "user-service", "users", orderRequest.getUserId().toString());
        Boolean isUserExists = checkExists(userUri);

        if (isUserExists != null && isUserExists.equals(false)) {
            throw new NotFoundException(NotFoundEnumException.USER);
        }

        Set<OrderItemRequest> orderItemsRequest = orderRequest.getOrderItem();
        Set<OrderItemPlaced> orderItemPlacedSet = new HashSet<>();

        for (OrderItemRequest orderItem : orderItemsRequest) {
            // request to product service to retrieve the product name
            String productUri = String.format("http://product-service/api/v1/products/%d", orderItem.getProductId());
            ProductResponse product = webClientBuilder.build().get()
                    .uri(productUri)
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .block();

            // throw exception if product not found
            if (product == null) {
                throw new NotFoundException(NotFoundEnumException.PRODUCT);
            }

            log.info(product.toString());

            // update the product price to order item request
            orderItem.setPrice(new BigDecimal(product.getPrice()));

            // add to orderItemPlaced Set
            OrderItemPlaced orderItemPlaced = new OrderItemPlaced();
            orderItemPlaced.setProductName(product.getName());
            orderItemPlaced.setPrice(new BigDecimal(product.getPrice()));
            orderItemPlaced.setQuantity(orderItem.getQuantity());

            orderItemPlacedSet.add(orderItemPlaced);
        }

        OrderModel orderModel = convertOrderRequestToModel(orderRequest);

        // calculate total amount
        BigDecimal totalAmount = orderRequest.getOrderItem().stream()
                .map(orderItem ->
                        orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // update total amount
        orderModel.setTotalAmount(totalAmount);

        // store to db and get the order back
        OrderModel orderSaved = orderRepository.save(orderModel);

        // store all order item to db
        orderRequest.getOrderItem().forEach(orderItem -> {
            // store order item
            orderItem.setOrder(orderSaved);
            orderItemService.createOrderItem(orderItem);
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
    }

    public List<OrderGraphQLResponse> getAllOrdersGraphQL() {
        List<OrderGraphQLResponse> orderGraphQLResponsesList = new ArrayList<>();
        List<OrderModel> orderModelList = orderRepository.findAll();

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

        // User Request
        String userBaseURL = "http://user-service/api/v1/users";

        for (OrderModel orderModel : orderModelList) {
            List<OrderItemGraphQLResponse> orderItemGraphQLResponses = new ArrayList<>();

            List<OrderItemResponse> orderItemModelList = orderItemService.getOrderItemsByOrderId(orderModel.getId());
            for (OrderItemResponse orderItem : orderItemModelList) {

                // Request to product service using graphQL query to retrieve product response
                ProductResponse product = graphQlClient
                        .document(productGraphQLQuery)
                        .operationName(productGraphQLOperationName)
                        .variable(productGraphQLVariableName, orderItem.getProductId())
                        .retrieve("productById") // which is the product response
                        .toEntity(ProductResponse.class).block();

                // throw exception if product not found
                if (product == null) {
                    throw new NotFoundException(NotFoundEnumException.PRODUCT);
                }
                orderItemGraphQLResponses.add(orderItemService.convertOrderItemResponseToGraphQLResponse(orderItem, product));
            }

            // request to user service to retrieve the user response
            String userUri = String.format("%s/%d", userBaseURL, orderModel.getUserId());
            UserResponse user = webClientBuilder.build().get()
                    .uri(userUri)
                    .retrieve()
                    .bodyToMono(UserResponse.class)
                    .block();

            // throw exception if user not found
            if (user == null) {
                throw new NotFoundException(NotFoundEnumException.USER);
            }

            // convert order item list to order item set
            orderGraphQLResponsesList.add(convertOrderModelToGraphQLResponse(orderModel, orderItemGraphQLResponses, user));
        }

        return orderGraphQLResponsesList;
    }

    private OrderGraphQLResponse convertOrderModelToGraphQLResponse(OrderModel orderModel, List<OrderItemGraphQLResponse> orderItemSet, UserResponse userResponse) {
        return OrderGraphQLResponse.builder()
                .id(orderModel.getId())
                .status(orderModel.getStatus())
                .totalAmount(String.valueOf(orderModel.getTotalAmount()))
                .orderItems(orderItemSet)
                .user(userResponse)
                .createdAt(orderModel.getCreatedAt().toString())
                .updatedAt(orderModel.getUpdatedAt().toString())
                .build();
    }

    private OrderModel convertOrderRequestToModel(OrderRequest orderRequest) {
        return OrderModel.builder()
                .userId(orderRequest.getUserId())
                .status(orderRequest.getStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Boolean checkExists(String uri) {
        return webClientBuilder.build().get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }
}
