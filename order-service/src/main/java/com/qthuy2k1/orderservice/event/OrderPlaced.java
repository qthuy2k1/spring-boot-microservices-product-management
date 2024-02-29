package com.qthuy2k1.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderPlaced {
    private String status;
    private String totalAmount;
    private String createdAt;
    private String updatedAt;
    private Set<OrderItemPlaced> orderItems;
}
