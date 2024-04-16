package com.inventoryservice.repository;

import com.inventoryservice.model.InventoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<InventoryModel, Integer> {
    List<InventoryModel> findBySkuCodeIn(List<String> skuCode);
}
