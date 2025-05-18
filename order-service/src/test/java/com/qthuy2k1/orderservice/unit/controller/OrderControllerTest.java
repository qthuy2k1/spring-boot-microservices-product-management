package com.qthuy2k1.orderservice.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.orderservice.controller.OrderController;
import com.qthuy2k1.orderservice.dto.request.OrderItemRequest;
import com.qthuy2k1.orderservice.dto.request.OrderRequest;
import com.qthuy2k1.orderservice.dto.response.ApiResponse;
import com.qthuy2k1.orderservice.dto.response.OrderItemResponse;
import com.qthuy2k1.orderservice.dto.response.OrderResponse;
import com.qthuy2k1.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@WithMockUser(username = "usertest", roles = "admin")
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private OrderService orderService;
    @InjectMocks
    private OrderController orderController;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {
        //Init MockMvc Object and build
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void createOrder() throws Exception {
        // given
        OrderItemRequest orderItemRequest = OrderItemRequest.builder()
                .productId(1)
                .quantity(2)
                .price(BigDecimal.valueOf(1000))
                .skuCode("abc")
                .build();
        Set<OrderItemRequest> orderItemRequestSet = Set.of(orderItemRequest);
        OrderRequest orderRequest = OrderRequest.builder()
                .status("PENDING")
                .orderItem(orderItemRequestSet)
                .build();
        OrderItemResponse orderItemResponse = OrderItemResponse.builder()
                .id(1)
                .price(BigDecimal.valueOf(1000))
                .productId(1)
                .quantity(2)
                .build();
        OrderResponse orderResponse = OrderResponse.builder()
                .id(1)
                .status("PENDING")
                .totalAmount(BigDecimal.valueOf(1000))
                .orderItems(Set.of(orderItemResponse))
                .build();

        given(orderService.createOrder(orderRequest)).willReturn(orderResponse);

        // when
        ApiResponse<OrderResponse> apiResponse = ApiResponse.<OrderResponse>builder()
                .result(orderResponse)
                .build();
        MvcResult result = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        // then
        then(orderService).should().createOrder(any());

        assertThat(result.getResponse().getContentAsString()).
                isEqualTo(objectMapper.writeValueAsString(apiResponse));
    }
}
