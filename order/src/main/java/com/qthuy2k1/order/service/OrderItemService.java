package com.qthuy2k1.order.service;

import com.qthuy2k1.order.dto.OrderItemRequest;
import com.qthuy2k1.order.dto.OrderItemResponse;
import com.qthuy2k1.order.model.OrderItemModel;
import com.qthuy2k1.order.repository.OrderItemRepository;
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
        System.out.println(orderItemModels);

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

    private OrderItemResponse convertOrderItemModelToResponse(OrderItemModel orderItemModel) {
        return OrderItemResponse.builder()
                .id(orderItemModel.getId())
                .productId(orderItemModel.getProductId())
                .price(orderItemModel.getPrice())
                .quantity(orderItemModel.getQuantity())
                .build();
    }
}
