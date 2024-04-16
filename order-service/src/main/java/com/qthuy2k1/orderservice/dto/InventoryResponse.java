package com.qthuy2k1.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryResponse {
    @NotEmpty(message = "the skuCode shouldn't be null")
    private String skuCode;
    @Min(value = 0, message = "the quantity should be greater than 0")
    private boolean isInStock;
}