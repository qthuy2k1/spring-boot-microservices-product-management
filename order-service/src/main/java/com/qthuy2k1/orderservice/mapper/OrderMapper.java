package com.qthuy2k1.orderservice.mapper;

import com.qthuy2k1.orderservice.dto.request.OrderRequest;
import com.qthuy2k1.orderservice.dto.response.OrderGraphQLResponse;
import com.qthuy2k1.orderservice.dto.response.OrderResponse;
import com.qthuy2k1.orderservice.model.OrderModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderModel toOder(OrderRequest request);

    OrderResponse toOrderResponse(OrderModel orderModel);

    OrderGraphQLResponse toOrderGraphQLResponse(OrderModel orderModel);
}
