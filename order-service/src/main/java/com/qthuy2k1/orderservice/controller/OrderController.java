package com.qthuy2k1.orderservice.controller;

import com.qthuy2k1.orderservice.dto.request.OrderRequest;
import com.qthuy2k1.orderservice.dto.response.ApiResponse;
import com.qthuy2k1.orderservice.dto.response.MessageResponse;
import com.qthuy2k1.orderservice.dto.response.OrderResponse;
import com.qthuy2k1.orderservice.dto.response.ReportResponse;
import com.qthuy2k1.orderservice.service.IOrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/orders")
@Slf4j
public class OrderController {
    IOrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@RequestBody @Valid OrderRequest orderRequest) throws ExecutionException, InterruptedException {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<OrderResponse>builder()
                        .result(orderService.createOrder(orderRequest))
                        .build()
        );
    }

    @PutMapping("{id}")
    public ResponseEntity<String> updateOrder(@PathVariable("id") int id, @RequestBody OrderRequest orderRequest) {
        orderService.updateOrder(id, orderRequest);
        return new ResponseEntity<>(MessageResponse.SUCCESS, HttpStatus.OK);
    }

    @GetMapping("/report")
    public ResponseEntity<ApiResponse<ReportResponse>> getReport(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    ) {
        return ResponseEntity.ok().body(
                ApiResponse.<ReportResponse>builder()
                        .result(orderService.getReport(startDate, endDate))
                        .build()
        );
    }
}
