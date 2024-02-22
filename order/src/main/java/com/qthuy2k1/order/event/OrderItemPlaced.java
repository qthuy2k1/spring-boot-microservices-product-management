package com.qthuy2k1.order.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderItemPlaced {
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
