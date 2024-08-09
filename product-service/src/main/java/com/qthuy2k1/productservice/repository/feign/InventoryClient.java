package com.qthuy2k1.productservice.repository.feign;

import com.qthuy2k1.productservice.config.AuthenticationRequestInterceptor;
import com.qthuy2k1.productservice.dto.request.InventoryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "INVENTORY-SERVICE", configuration = {AuthenticationRequestInterceptor.class})
public interface InventoryClient {
    @PostMapping("/inventories")
    void createInventory(@RequestBody InventoryRequest inventoryRequest);
}
