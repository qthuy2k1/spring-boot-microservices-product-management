package com.qthuy2k1.orderservice.dto;

import lombok.Data;

@Data
public class ProductResponse {
    private Integer id;
    private String name;
    private String description;
    private String price;
    private ProductCategoryResponse category;
    private String skuCode;
}
