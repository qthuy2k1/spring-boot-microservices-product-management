package com.qthuy2k1.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
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
