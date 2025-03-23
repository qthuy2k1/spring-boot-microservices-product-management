package com.qthuy2k1.paymentservice.service;

import com.qthuy2k1.paymentservice.dto.OrderRequest;
import com.qthuy2k1.paymentservice.feign.IOrderClient;
import com.qthuy2k1.paymentservice.model.PaymentModel;
import com.qthuy2k1.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {
    private final PaymentRepository paymentRepository;
    private final IOrderClient orderClient;

    public void createPayment(PaymentModel paymentModel) {
        paymentRepository.save(paymentModel);
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setStatus("PAID");

        // Produce kafka message to order client
        orderClient.updateOrder(paymentModel.getOrderID().toString(), orderRequest);

        // Produce kafka message to notification client to send email
    }
}
