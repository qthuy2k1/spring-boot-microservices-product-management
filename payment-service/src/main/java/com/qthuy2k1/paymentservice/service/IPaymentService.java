package com.qthuy2k1.paymentservice.service;

import com.qthuy2k1.paymentservice.dto.OrderPaymentRequest;
import com.qthuy2k1.paymentservice.dto.PaymentResponse;
import com.qthuy2k1.paymentservice.dto.TransactionStatusResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

public interface IPaymentService {
    PaymentResponse getUrl(OrderPaymentRequest orderPaymentRequest);

    TransactionStatusResponse transaction(
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
    );
//    void createPayment(PaymentModel paymentModel);
}
