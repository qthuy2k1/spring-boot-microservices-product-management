package com.qthuy2k1.productservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "products_tbl")
@FieldDefaults(level = AccessLevel.PRIVATE)
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
    Integer id;
    @NotBlank(message = "PRODUCT_NAME_BLANK")
    String name;
    @NotBlank(message = "PRODUCT_DESCRIPTION_BLANK")
    String description;
    @PositiveOrZero(message = "PRODUCT_PRICE_MIN")
    BigDecimal price;
    @NotBlank(message = "PRODUCT_SKUCODE_BLANK")
    String skuCode;
    @ManyToOne
    @JoinColumn(name = "category_id")
    ProductCategoryModel category;
    String thumbnail;
    String url;
}

