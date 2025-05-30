package com.inventoryservice.controller;

import com.inventoryservice.dto.request.InventoryRequest;
import com.inventoryservice.dto.response.InventoryResponse;
import com.inventoryservice.service.IInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventories")
@Slf4j
public class InventoryController {
    private final IInventoryService inventoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createInventory(@Valid @RequestBody InventoryRequest inventoryRequest) {
        inventoryService.createInventory(inventoryRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse isInStock(
            @RequestParam("quantity") Integer quantity, @RequestParam("productId") Integer productId) {
        return inventoryService.isInStock(quantity, productId);
    }

    @PostMapping("/update-product-stock")
    @ResponseStatus(HttpStatus.OK)
    public String updateProductStock(InventoryRequest inventoryRequest) throws Exception {
        inventoryService.updateProductStock(inventoryRequest);
        return "SUCCESS";
    }
}
