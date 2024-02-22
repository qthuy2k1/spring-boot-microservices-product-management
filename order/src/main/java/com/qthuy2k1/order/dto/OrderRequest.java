package com.qthuy2k1.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    @Min(value = 1, message = "user id must greater or equals to 1")
    @NotNull(message = "user id shouldn't be null")
    private Integer userId;
    @NotEmpty(message = "status shouldn't be null")
    private String status;
    private Set<OrderItemRequest> orderItem;
}

