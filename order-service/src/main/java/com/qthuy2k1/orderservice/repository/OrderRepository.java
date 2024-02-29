package com.qthuy2k1.orderservice.repository;

import com.qthuy2k1.orderservice.model.OrderModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderModel, Integer> {
}
