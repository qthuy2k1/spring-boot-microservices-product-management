package com.qthuy2k1.orderservice.controller;

import com.qthuy2k1.orderservice.dto.OrderGraphQLResponse;
import com.qthuy2k1.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OrderGraphQLController {
    private final OrderService orderService;

    @QueryMapping
    public List<OrderGraphQLResponse> getOrders() {
        return orderService.getAllOrdersGraphQL();
    }
}
