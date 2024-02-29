package com.qthuy2k1.orderservice.service;

import com.qthuy2k1.orderservice.dto.OrderItemRequest;
import com.qthuy2k1.orderservice.dto.OrderRequest;
import com.qthuy2k1.orderservice.dto.ProductResponse;
import com.qthuy2k1.orderservice.event.OrderItemPlaced;
import com.qthuy2k1.orderservice.event.OrderPlaced;
import com.qthuy2k1.orderservice.exception.NotFoundEnumException;
import com.qthuy2k1.orderservice.exception.NotFoundException;
import com.qthuy2k1.orderservice.model.OrderModel;
import com.qthuy2k1.orderservice.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
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

            // update the product price to order item request
            orderItem.setPrice(product.getPrice());

            // add to orderItemPlaced Set
            OrderItemPlaced orderItemPlaced = new OrderItemPlaced();
            orderItemPlaced.setProductName(product.getName());
            orderItemPlaced.setPrice(product.getPrice());
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
