package com.qthuy2k1.order.repository;

import com.qthuy2k1.order.model.OrderModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderModel, Integer> {
}
