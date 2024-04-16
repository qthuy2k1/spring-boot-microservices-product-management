package com.qthuy2k1.productservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class InventoryRequest {
    @NotEmpty(message = "the skuCode shouldn't be null")
    private String skuCode;
    @Min(value = 0, message = "the quantity should be greater than 0")
    private Integer quantity;
}
