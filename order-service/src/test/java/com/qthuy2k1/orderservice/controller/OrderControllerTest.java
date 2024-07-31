package com.qthuy2k1.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.orderservice.dto.request.OrderItemRequest;
import com.qthuy2k1.orderservice.dto.request.OrderRequest;
import com.qthuy2k1.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private OrderService orderService;
    @InjectMocks
    private OrderController orderController;

    @Test
    void createOrder() throws Exception {
        // given
        OrderItemRequest orderItemRequest = new OrderItemRequest(1, 2, BigDecimal.valueOf(1000), "abc", null);
        Set<OrderItemRequest> orderItemRequestSet = Set.of(orderItemRequest);
        OrderRequest orderRequest = new OrderRequest(1, "PENDING", orderItemRequestSet);
        String orderRequestString = objectMapper.writeValueAsString(orderRequest);

        // when
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestString))
                .andExpect(status().isCreated())
                .andExpect(content().string("Success"))
                .andDo(print());

        // then
        verify(orderService).createOrder(any());
    }
}
