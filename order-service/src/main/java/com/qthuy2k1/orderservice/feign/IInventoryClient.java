package com.qthuy2k1.orderservice.feign;

import com.qthuy2k1.orderservice.dto.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("INVENTORY-SERVICE")
public interface IInventoryClient {
    @GetMapping("/api/v1/inventories")
    InventoryResponse isInStock(@RequestParam("quantity") Integer quantity, @RequestParam("productId") Integer productId);
}
