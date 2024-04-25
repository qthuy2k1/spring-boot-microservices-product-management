package com.qthuy2k1.orderservice.feign;

import com.qthuy2k1.orderservice.config.MyOpenFeignConfig;
import com.qthuy2k1.orderservice.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "PRODUCT-SERVICE", configuration = MyOpenFeignConfig.class)
public interface IProductClient {
    @GetMapping("/api/v1/products/{id}")
    ResponseEntity<ProductResponse> getProduct(@PathVariable("id") String id);
}
