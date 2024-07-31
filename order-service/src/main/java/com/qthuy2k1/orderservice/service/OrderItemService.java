package com.qthuy2k1.orderservice.service;

import com.qthuy2k1.orderservice.dto.request.OrderItemRequest;
import com.qthuy2k1.orderservice.dto.response.OrderItemResponse;
import com.qthuy2k1.orderservice.mapper.OrderItemMapper;
import com.qthuy2k1.orderservice.model.OrderItemModel;
import com.qthuy2k1.orderservice.repository.OrderItemRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderItemService {
    OrderItemRepository orderItemRepository;
    OrderItemMapper orderItemMapper;


    public OrderItemModel createOrderItem(OrderItemRequest orderItemRequest) {
        OrderItemModel orderItemModel = orderItemMapper.toOrderItem(orderItemRequest);

        return orderItemRepository.save(orderItemModel);
    }

    public List<OrderItemResponse> getOrderItemsByOrderId(Integer orderId) {
        List<OrderItemModel> orderItemModels = orderItemRepository.findAllByOrderId(orderId);
        return orderItemModels.stream().map(orderItemMapper::toOrderItemResponse).toList();
    }
}
