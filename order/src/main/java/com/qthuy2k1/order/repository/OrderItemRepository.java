package com.qthuy2k1.order.repository;

import com.qthuy2k1.order.model.OrderItemModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemModel, Integer> {
    List<OrderItemModel> findAllByOrderId(Integer id);
}
