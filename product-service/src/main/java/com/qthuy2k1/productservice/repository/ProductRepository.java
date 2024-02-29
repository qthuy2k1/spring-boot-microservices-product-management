package com.qthuy2k1.productservice.repository;

import com.qthuy2k1.productservice.model.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductModel, Integer> {
}
