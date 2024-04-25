package com.inventoryservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "inventories_tbl")
@Builder
public class InventoryModel {
    @Id
    @SequenceGenerator(
            name = "inventory_id_sequence",
            sequenceName = "inventory_id_sequence"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "inventory_id_sequence"
    )
    private Integer id;
    @Min(value = 1, message = "the product id should be greater than 0")
    private Integer productId;
    @Min(value = 0, message = "the quantity should be greater than 0")
    private Integer quantity;
}
