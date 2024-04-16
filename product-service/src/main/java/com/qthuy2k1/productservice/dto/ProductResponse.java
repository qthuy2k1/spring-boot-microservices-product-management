package com.qthuy2k1.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductResponse implements Serializable {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private ProductCategoryResponse category;
}
