package com.inventoryservice.service;

import com.inventoryservice.dto.InventoryRequest;
import com.inventoryservice.dto.InventoryResponse;

public interface IInventoryService {
    void createInventory(InventoryRequest inventoryRequest);

    InventoryResponse isInStock(Integer quantity, Integer productId);
}
