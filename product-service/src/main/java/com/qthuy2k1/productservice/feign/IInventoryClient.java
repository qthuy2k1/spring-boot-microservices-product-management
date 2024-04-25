package com.qthuy2k1.productservice.feign;

import com.qthuy2k1.productservice.dto.InventoryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("INVENTORY-SERVICE")
public interface IInventoryClient {
    @PostMapping("/api/v1/inventories")
    void createInventory(@RequestBody InventoryRequest inventoryRequest);
}
