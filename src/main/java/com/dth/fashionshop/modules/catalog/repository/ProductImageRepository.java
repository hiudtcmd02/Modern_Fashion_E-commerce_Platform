package com.dth.fashionshop.modules.catalog.repository;

import com.dth.fashionshop.modules.catalog.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}