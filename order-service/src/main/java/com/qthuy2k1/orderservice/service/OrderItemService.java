package com.qthuy2k1.orderservice.service;

import com.qthuy2k1.orderservice.dto.OrderItemGraphQLResponse;
import com.qthuy2k1.orderservice.dto.OrderItemRequest;
import com.qthuy2k1.orderservice.dto.OrderItemResponse;
import com.qthuy2k1.orderservice.dto.ProductResponse;
import com.qthuy2k1.orderservice.model.OrderItemModel;
import com.qthuy2k1.orderservice.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;

    public OrderItemModel createOrderItem(OrderItemRequest orderItemRequest) {
        OrderItemModel orderItemModel = convertOrderItemRequestToModel(orderItemRequest);

        return orderItemRepository.save(orderItemModel);
    }

    public List<OrderItemResponse> getOrderItemsByOrderId(Integer orderId) {
        List<OrderItemModel> orderItemModels = orderItemRepository.findAllByOrderId(orderId);
        return orderItemModels.stream().map(this::convertOrderItemModelToResponse).toList();
    }

    private OrderItemModel convertOrderItemRequestToModel(OrderItemRequest orderItemRequest) {
        return OrderItemModel.builder()
                .productId(orderItemRequest.getProductId())
                .price(orderItemRequest.getPrice())
                .quantity(orderItemRequest.getQuantity())
                .order(orderItemRequest.getOrder())
                .build();
    }

    public OrderItemResponse convertOrderItemModelToResponse(OrderItemModel orderItemModel) {
        return OrderItemResponse.builder()
                .id(orderItemModel.getId())
                .productId(orderItemModel.getProductId())
                .price(orderItemModel.getPrice())
                .quantity(orderItemModel.getQuantity())
                .build();
    }

    public OrderItemGraphQLResponse convertOrderItemModelToGraphQLResponse(OrderItemModel orderItemModel, ProductResponse productResponse) {
        return OrderItemGraphQLResponse.builder()
                .id(orderItemModel.getId())
                .product(productResponse)
                .price(String.valueOf(orderItemModel.getPrice()))
                .quantity(orderItemModel.getQuantity())
                .build();
    }

    public OrderItemGraphQLResponse convertOrderItemResponseToGraphQLResponse(OrderItemResponse orderItem, ProductResponse productResponse) {
        return OrderItemGraphQLResponse.builder()
                .id(orderItem.getId())
                .product(productResponse)
                .price(String.valueOf(orderItem.getPrice()))
                .quantity(orderItem.getQuantity())
                .build();
    }
}
