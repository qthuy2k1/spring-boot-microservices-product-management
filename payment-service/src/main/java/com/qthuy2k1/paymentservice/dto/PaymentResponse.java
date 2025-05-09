package com.qthuy2k1.paymentservice.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PaymentResponse implements Serializable {
    private String status;
    private String message;
    private String URL;
}
