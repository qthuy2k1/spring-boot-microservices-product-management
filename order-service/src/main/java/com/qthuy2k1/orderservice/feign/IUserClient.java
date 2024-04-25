package com.qthuy2k1.orderservice.feign;

import com.qthuy2k1.orderservice.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("USER-SERVICE")
public interface IUserClient {
    @GetMapping("/api/v1/users/{id}")
    UserResponse getUser(@PathVariable("id") String id);

    @GetMapping("/api/v1/users/{id}/is-exists")
    Boolean existsById(@PathVariable("id") String id);
}
