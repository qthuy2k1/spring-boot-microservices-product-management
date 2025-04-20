package com.qthuy2k1.orderservice.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ReduceInventoryRequest {
    @Min(value = 1, message = "the 'product id' should be greater than 0")
    private Integer productId;
    @Min(value = 0, message = "the 'reduce by' should be greater than or equal to 0")
    private Integer reduceBy;
}
