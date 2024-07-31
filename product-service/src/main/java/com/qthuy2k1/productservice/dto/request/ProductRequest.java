package com.qthuy2k1.productservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {
    @NotBlank(message = "PRODUCT_NAME_BLANK")
    String name;
    @NotBlank(message = "PRODUCT_DESCRIPTION_BLANK")
    String description;
    @PositiveOrZero(message = "PRODUCT_PRICE_MIN")
    BigDecimal price;
    @Positive(message = "PRODUCT_CATEGORY_MIN")
    Integer categoryId;
    @NotBlank(message = "PRODUCT_SKUCODE_BLANK")
    String skuCode;
    @PositiveOrZero(message = "PRODUCT_QUANTITY_MIN")
    Integer quantity;
}
