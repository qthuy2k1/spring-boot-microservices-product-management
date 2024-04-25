package com.inventoryservice.repository;

import com.inventoryservice.model.InventoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryModel, Integer> {
    Optional<InventoryModel> findByProductId(Integer productId);
}
