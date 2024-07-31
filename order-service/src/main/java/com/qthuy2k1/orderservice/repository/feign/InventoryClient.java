package com.qthuy2k1.orderservice.repository.feign;

import com.qthuy2k1.orderservice.dto.response.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("INVENTORY-SERVICE")
public interface InventoryClient {
    @GetMapping("/inventories")
    InventoryResponse isInStock(@RequestParam("quantity") Integer quantity, @RequestParam("productId") Integer productId);
}
