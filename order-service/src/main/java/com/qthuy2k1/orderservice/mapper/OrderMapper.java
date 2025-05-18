package com.qthuy2k1.orderservice.mapper;

import com.qthuy2k1.orderservice.dto.request.OrderRequest;
import com.qthuy2k1.orderservice.dto.response.OrderGraphQLResponse;
import com.qthuy2k1.orderservice.dto.response.OrderResponse;
import com.qthuy2k1.orderservice.enums.OrderStatus;
import com.qthuy2k1.orderservice.model.OrderModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "status", source = "status", qualifiedByName = "mapOrderStatus")
    OrderModel toOder(OrderRequest request);

    OrderResponse toOrderResponse(OrderModel orderModel);

    OrderGraphQLResponse toOrderGraphQLResponse(OrderModel orderModel);

    @Named("mapOrderStatus")
    default OrderStatus mapOrderStatus(String label) {
        return OrderStatus.fromLabel(label); // your custom method
    }
}
