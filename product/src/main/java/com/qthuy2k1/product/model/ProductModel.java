package com.qthuy2k1.product.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "products_tbl")
public class ProductModel {
    @Id
    @SequenceGenerator(
            name = "product_id_sequence",
            sequenceName = "product_id_sequence"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "product_id_sequence"
    )
    private Integer id;
    @NotEmpty(message = "product name shouldn't be null")
    private String name;
    @NotEmpty(message = "product description shouldn't be null")
    private String description;
    @Min(1)
    private BigDecimal price;
    @Min(1)
    private Integer userId;
    @NotEmpty(message = "skuCode shouldn't be null")
    private String skuCode;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private ProductCategoryModel category;
}