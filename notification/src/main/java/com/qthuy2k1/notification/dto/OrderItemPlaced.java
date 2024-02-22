package com.qthuy2k1.notification.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderItemPlaced {
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
