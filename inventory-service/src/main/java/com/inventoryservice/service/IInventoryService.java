package com.inventoryservice.service;

import com.inventoryservice.dto.request.InventoryRequest;
import com.inventoryservice.dto.response.InventoryResponse;

public interface IInventoryService {
    void createInventory(InventoryRequest inventoryRequest);

    InventoryResponse isInStock(Integer quantity, Integer productId);

    void updateProductStock(InventoryRequest inventoryRequest) throws Exception;
}
