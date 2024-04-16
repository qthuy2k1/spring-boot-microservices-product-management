package com.qthuy2k1.orderservice.dto;

import com.qthuy2k1.orderservice.model.OrderModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequest {
    private Integer productId;
    private Integer quantity;
    private BigDecimal price;
    private String skuCode;
    private OrderModel order;
}
