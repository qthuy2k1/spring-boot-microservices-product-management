package com.qthuy2k1.productservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductRequest {
    @NotBlank(message = "the product name shouldn't be blank")
    private String name;
    @NotBlank(message = "the product description shouldn't be blank")
    private String description;
    @Min(value = 1, message = "the product price should greater than or equal to 1")
    @NotNull(message = "the product price shouldn't be null")
    private BigDecimal price;
    @NotNull(message = "the product category id shouldn't be null")
    private Integer categoryId;
    @NotBlank(message = "the sku code shouldn't be blank")
    private String skuCode;
    @NotNull(message = "the product quantity shouldn't be null")
    @Min(value = 0, message = "the product quantity should greater than or equal to 0")
    private Integer quantity;
}
