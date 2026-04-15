package com.dth.fashionshop.modules.cart.repository;

import com.dth.fashionshop.modules.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByIdAndCart_Id(Long id, Long cartId);
}