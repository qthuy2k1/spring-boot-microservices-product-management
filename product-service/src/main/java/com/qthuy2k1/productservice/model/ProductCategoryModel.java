package com.qthuy2k1.productservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Builder
@Table(name = "product_categories_tbl")
public class ProductCategoryModel {
    @Id
    @SequenceGenerator(
            name = "product_category_id_sequence",
            sequenceName = "product_category_id_sequence"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "product_category_id_sequence"
    )
    private Integer id;
    private String name;
    private String description;
    @OneToMany(mappedBy = "category")
    private Set<ProductModel> products;
}
