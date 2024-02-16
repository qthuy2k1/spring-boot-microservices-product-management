package com.qthuy2k1.product.repository;

import com.qthuy2k1.product.model.ProductCategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategoryModel, Integer> {
}
