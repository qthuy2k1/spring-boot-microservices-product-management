package com.qthuy2k1.notificationservice.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderItemPlaced {
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
