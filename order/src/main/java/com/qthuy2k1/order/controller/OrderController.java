package com.qthuy2k1.order.controller;

import com.qthuy2k1.order.dto.OrderRequest;
import com.qthuy2k1.order.exception.NotFoundException;
import com.qthuy2k1.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody @Valid OrderRequest orderRequest) throws NotFoundException {
       orderService.createOrder(orderRequest);
       return new ResponseEntity<>("Success", HttpStatus.CREATED);
    }
}
