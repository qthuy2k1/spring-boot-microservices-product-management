package com.qthuy2k1.productservice.repository;

import com.qthuy2k1.productservice.model.ProductCategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategoryModel, Integer> {
}
