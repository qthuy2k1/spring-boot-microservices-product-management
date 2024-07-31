package com.qthuy2k1.orderservice.controller;

import com.qthuy2k1.orderservice.dto.response.OrderGraphQLResponse;
import com.qthuy2k1.orderservice.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderGraphQLController {
    OrderService orderService;

    @QueryMapping
    public List<OrderGraphQLResponse> getOrders() {
        return orderService.getAllOrdersGraphQL();
    }
}
