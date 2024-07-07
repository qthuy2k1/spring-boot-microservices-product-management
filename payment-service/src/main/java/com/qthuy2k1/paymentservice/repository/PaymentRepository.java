package com.qthuy2k1.paymentservice.repository;

import com.qthuy2k1.paymentservice.model.PaymentModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<PaymentModel, Integer> {
}
