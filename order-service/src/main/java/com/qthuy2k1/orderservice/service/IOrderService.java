package com.qthuy2k1.orderservice.service;

import com.qthuy2k1.orderservice.dto.request.OrderRequest;
import com.qthuy2k1.orderservice.dto.request.UpdatePaidOrderRequest;
import com.qthuy2k1.orderservice.dto.request.UpdateStatusOrderRequest;
import com.qthuy2k1.orderservice.dto.response.OrderGraphQLResponse;
import com.qthuy2k1.orderservice.dto.response.OrderResponse;
import com.qthuy2k1.orderservice.dto.response.ReportResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface IOrderService {

    OrderResponse createOrder(OrderRequest orderRequest) throws ExecutionException, InterruptedException;

    OrderResponse createOrderFallback(OrderRequest orderRequest, Throwable throwable);

    List<OrderGraphQLResponse> getAllOrdersGraphQL();

    void updateStatusOrder(Integer id, UpdateStatusOrderRequest orderRequest);

    void handleKafkaPaidOrderUpdate(Integer id, UpdatePaidOrderRequest orderRequest);

    ReportResponse getReport(String startDate, String endDate);
}
