package com.dth.fashionshop.modules.catalog.repository;

import com.dth.fashionshop.modules.catalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    long countByCategory_IdAndIsDeletedFalse(Long categoryId);
}