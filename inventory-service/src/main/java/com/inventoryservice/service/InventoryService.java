package com.inventoryservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventoryservice.dto.InventoryRequest;
import com.inventoryservice.dto.InventoryResponse;
import com.inventoryservice.dto.ReduceInventoryRequest;
import com.inventoryservice.model.InventoryModel;
import com.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService implements IInventoryService {
    private final InventoryRepository inventoryRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public void createInventory(InventoryRequest inventoryRequest) {
        inventoryRepository.save(
                InventoryModel.builder()
                        .quantity(inventoryRequest.getQuantity())
                        .productId(inventoryRequest.getProductId())
                        .build()
        );
    }

    @KafkaListener(topics = "create-inventory", groupId = "inventory-group")
    public void createInventoryKafka(LinkedHashMap<String, Object> rawInventoryRequest) {
        log.info("receive message from kafka: create inventory");
        InventoryRequest inventoryRequest = mapper.convertValue(rawInventoryRequest, InventoryRequest.class);
        inventoryRepository.save(
                InventoryModel.builder()
                        .quantity(inventoryRequest.getQuantity())
                        .productId(inventoryRequest.getProductId())
                        .build()
        );
    }

    @KafkaListener(topics = "create-inventory-list", groupId = "inventory-list-group")
    public void createInventoryListKafka(List<LinkedHashMap<String, Object>> rawInventoryListRequest) {
        log.info("receive message from kafka: create inventory list");
        List<InventoryRequest> inventoryRequests = rawInventoryListRequest.stream()
                .map(map -> mapper.convertValue(map, InventoryRequest.class))
                .toList();
        List<InventoryModel> batchInsertInventories = new ArrayList<>();
        // Convert to inventory model to save all later
        inventoryRequests.forEach(inventory -> {
            batchInsertInventories.add(
                    InventoryModel.builder()
                            .quantity(inventory.getQuantity())
                            .productId(inventory.getProductId())
                            .build()
            );
        });

        inventoryRepository.saveAll(batchInsertInventories);
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

    @KafkaListener(topics = "reduce-product-stock", groupId = "reduce-product-stock-group")
    public void reduceProductStock(List<LinkedHashMap<String, Object>> rawReduceProductStockListRequest) {
        log.info("REDUCE PRODUCT STOCK");
        List<ReduceInventoryRequest> reduceRequests = rawReduceProductStockListRequest.stream()
                .map(map -> mapper.convertValue(map, ReduceInventoryRequest.class))
                .toList();

        List<InventoryModel> updatedInventories = new ArrayList<>();
        reduceRequests.forEach(request -> {
            Optional<InventoryModel> inventoryOptional = inventoryRepository.findByProductId(request.getProductId());
            inventoryOptional.ifPresent(inventory -> {
                inventory.setQuantity(inventory.getQuantity() - request.getReduceBy());
                // Collect updated inventories to save later
                updatedInventories.add(inventory);
            });
        });
        // Save them all at once
        inventoryRepository.saveAll(updatedInventories);
        log.info("SAVED: {} ITEMS", updatedInventories.size());
    }

    public void updateProductStock(InventoryRequest inventoryRequest) throws Exception {
        InventoryModel inventory = inventoryRepository.findByProductId(inventoryRequest.getProductId())
                .orElseThrow(() -> new Exception("product not found"));
        inventory.setQuantity(inventoryRequest.getQuantity());
        inventoryRepository.save(inventory);
    }
}
