package com.qthuy2k1.productservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class InventoryRequest {
    @Min(value = 1, message = "the product id should be greater than 0")
    @NotNull(message = "the product id shouldn't be null")
    private Integer productId;
    @NotNull(message = "the quantity shouldn't be null")
    @Min(value = 0, message = "the quantity should be greater than or equal to 0")
    private Integer quantity;
}
