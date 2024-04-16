package com.inventoryservice.service;

import com.inventoryservice.dto.InventoryRequest;
import com.inventoryservice.dto.InventoryResponse;
import com.inventoryservice.model.InventoryModel;
import com.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public void createInventory(InventoryRequest inventoryRequest) {
        inventoryRepository.save(
                InventoryModel.builder()
                        .quantity(inventoryRequest.getQuantity())
                        .skuCode(inventoryRequest.getSkuCode())
                        .build()
        );
    }

    @Transactional(readOnly = true)
    @SneakyThrows
    public List<InventoryResponse> isInStock(List<String> skuCode) {
        log.info(skuCode.toString());
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map(inventory ->
                        InventoryResponse.builder()
                                .skuCode(inventory.getSkuCode())
                                .isInStock(inventory.getQuantity() > 0)
                                .build()
                ).toList();
    }
}
