package com.qthuy2k1.productservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductGraphQLResponse {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private String skuCode;
    private ProductCategoryResponse category;
    private String url;
    private String thumb;
}
