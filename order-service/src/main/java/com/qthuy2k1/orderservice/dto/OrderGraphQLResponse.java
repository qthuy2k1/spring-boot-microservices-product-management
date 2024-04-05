package com.qthuy2k1.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderGraphQLResponse {
    private Integer id;
    private UserResponse user;
    private String status;
    private String totalAmount;
    private String createdAt;
    private String updatedAt;
    private List<OrderItemGraphQLResponse> orderItems;
}
