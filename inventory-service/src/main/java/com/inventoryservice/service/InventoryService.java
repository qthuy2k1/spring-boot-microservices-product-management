package com.inventoryservice.service;

import com.inventoryservice.dto.InventoryRequest;
import com.inventoryservice.dto.InventoryResponse;
import com.inventoryservice.model.InventoryModel;
import com.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public void createInventory(InventoryRequest inventoryRequest) {
        inventoryRepository.save(
                InventoryModel.builder()
                        .quantity(inventoryRequest.getQuantity())
                        .productId(inventoryRequest.getProductId())
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public InventoryResponse isInStock(Integer quantity, Integer productId) {
        Optional<InventoryModel> inventoryOptional = inventoryRepository.findByProductId(productId);
        if (inventoryOptional.isEmpty()) {
            return InventoryResponse.builder()
                    .isInStock(false)
                    .build();
        }

        if (inventoryOptional.get().getQuantity() - quantity <= 0) {
            return InventoryResponse.builder()
                    .isInStock(false)
                    .build();
        }

        return InventoryResponse.builder()
                .isInStock(true)
                .build();
    }
}
