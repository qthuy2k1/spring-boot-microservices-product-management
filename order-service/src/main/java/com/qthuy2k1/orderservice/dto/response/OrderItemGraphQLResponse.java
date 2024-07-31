package com.qthuy2k1.orderservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemGraphQLResponse {
    private Integer id;
    private ProductResponse product;
    private Integer quantity;
    private String price;
}
