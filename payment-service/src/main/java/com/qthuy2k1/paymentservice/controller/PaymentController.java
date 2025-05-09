package com.qthuy2k1.paymentservice.controller;

import com.qthuy2k1.paymentservice.dto.OrderPaymentRequest;
import com.qthuy2k1.paymentservice.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final IPaymentService paymentService;

    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(@RequestBody OrderPaymentRequest orderPaymentRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(paymentService.getUrl(orderPaymentRequest));
    }

    @GetMapping("/payment-info")
    public ResponseEntity<?> transaction(
            @RequestParam("vnp_Amount") String amount,
            @RequestParam("vnp_BankCode") String bankCode,
            @RequestParam("vnp_BankTranNo") String bankTranNo,
            @RequestParam("vnp_CardType") String cardType,
            @RequestParam("vnp_PayDate") String payDate,
            @RequestParam("vnp_OrderInfo") String orderInfo,
            @RequestParam("vnp_TransactionNo") BigDecimal transactionNo,
            @RequestParam("vnp_TransactionStatus") Integer transactionStatus,
            @RequestParam("vnp_TxnRef") Integer orderID,
            @RequestParam("vnp_ResponseCode") String respCode
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                paymentService.transaction(
                        amount,
                        bankCode,
                        bankTranNo,
                        cardType,
                        payDate,
                        orderInfo,
                        transactionNo,
                        transactionStatus,
                        orderID,
                        respCode
                ));
    }
}
