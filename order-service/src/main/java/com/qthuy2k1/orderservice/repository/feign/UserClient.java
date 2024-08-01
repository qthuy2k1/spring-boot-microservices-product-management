package com.qthuy2k1.orderservice.repository.feign;

import com.qthuy2k1.orderservice.config.AuthenticationRequestInterceptor;
import com.qthuy2k1.orderservice.dto.response.ApiResponse;
import com.qthuy2k1.orderservice.dto.response.UserResponse;
import jakarta.validation.constraints.Email;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE", configuration = {AuthenticationRequestInterceptor.class})
public interface UserClient {
    @GetMapping("/users/email/{email}")
    ApiResponse<UserResponse> getUserByEmail(@PathVariable("email") @Email String email);

    @GetMapping("/users/{id}/is-exists")
    Boolean existsById(@PathVariable("id") String id);
}
