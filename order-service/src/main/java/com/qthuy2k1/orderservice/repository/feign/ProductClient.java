package com.qthuy2k1.orderservice.repository.feign;

import com.qthuy2k1.orderservice.config.AuthenticationRequestInterceptor;
import com.qthuy2k1.orderservice.config.MyOpenFeignConfig;
import com.qthuy2k1.orderservice.dto.response.ApiResponse;
import com.qthuy2k1.orderservice.dto.response.ProductResponse;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "PRODUCT-SERVICE",
        configuration = {MyOpenFeignConfig.class, AuthenticationRequestInterceptor.class})
public interface ProductClient {
    @GetMapping("/products/{id}")
    ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable("id") @Positive int id);
}
