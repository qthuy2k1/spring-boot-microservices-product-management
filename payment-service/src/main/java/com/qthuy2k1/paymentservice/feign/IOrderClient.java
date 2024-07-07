package com.qthuy2k1.paymentservice.feign;

import com.qthuy2k1.paymentservice.dto.OrderRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("ORDER-SERVICE")
public interface IOrderClient {
    @PutMapping("/api/v1/orders/{id}")
    void updateOrder(@PathVariable("id") String id, @RequestBody OrderRequest orderRequest);
}
