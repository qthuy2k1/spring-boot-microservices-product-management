package com.qthuy2k1.orderservice.service;

import com.qthuy2k1.orderservice.dto.request.OrderItemRequest;
import com.qthuy2k1.orderservice.dto.response.OrderItemResponse;
import com.qthuy2k1.orderservice.model.OrderItemModel;

import java.util.List;

public interface IOrderItemService {

    OrderItemModel createOrderItem(OrderItemRequest orderItemRequest);

    List<OrderItemResponse> getOrderItemsByOrderId(Integer orderId);
}
