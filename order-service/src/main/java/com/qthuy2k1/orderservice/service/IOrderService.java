package com.qthuy2k1.orderservice.service;

import com.qthuy2k1.orderservice.dto.request.OrderRequest;
import com.qthuy2k1.orderservice.dto.response.OrderGraphQLResponse;
import com.qthuy2k1.orderservice.dto.response.OrderResponse;
import com.qthuy2k1.orderservice.dto.response.ReportResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface IOrderService {

    OrderResponse createOrder(OrderRequest orderRequest) throws ExecutionException, InterruptedException;

    List<OrderGraphQLResponse> getAllOrdersGraphQL();

    void updateOrder(Integer id, OrderRequest orderRequest);

    ReportResponse getReport(String startDate, String endDate);
}
