package com.qthuy2k1.product.repository;

import com.qthuy2k1.product.model.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductModel, Integer> {
}
