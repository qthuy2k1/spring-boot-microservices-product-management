package com.qthuy2k1.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponse {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer userId;
    private Integer categoryId;
    private String skuCode;
}
