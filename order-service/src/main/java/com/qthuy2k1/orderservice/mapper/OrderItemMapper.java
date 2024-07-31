package com.qthuy2k1.orderservice.mapper;

import com.qthuy2k1.orderservice.dto.request.OrderItemRequest;
import com.qthuy2k1.orderservice.dto.response.OrderItemGraphQLResponse;
import com.qthuy2k1.orderservice.dto.response.OrderItemResponse;
import com.qthuy2k1.orderservice.model.OrderItemModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    OrderItemModel toOrderItem(OrderItemRequest request);

    OrderItemResponse toOrderItemResponse(OrderItemModel orderItemModel);

    OrderItemGraphQLResponse toOrderItemGraphQLResponse(OrderItemModel orderItemModel);
}
