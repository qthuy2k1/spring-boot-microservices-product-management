package com.qthuy2k1.paymentservice.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderPaymentDTO {
    private Integer orderId;
    private BigDecimal amount;
}
