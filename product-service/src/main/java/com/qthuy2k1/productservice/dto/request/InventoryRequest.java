package com.qthuy2k1.productservice.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryRequest {
    @Positive(message = "PRODUCT_ID_MIN")
    Integer productId;
    @PositiveOrZero(message = "PRODUCT_QUANTITY_MIN")
    Integer quantity;
}
