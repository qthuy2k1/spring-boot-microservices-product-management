package com.qthuy2k1.orderservice.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {
    private Integer id;
    private Integer productId;
    private Integer quantity;
    private BigDecimal price;
}
