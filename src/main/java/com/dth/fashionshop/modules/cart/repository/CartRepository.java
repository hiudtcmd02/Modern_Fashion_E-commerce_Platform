package com.dth.fashionshop.modules.cart.repository;

import com.dth.fashionshop.modules.cart.entity.Cart;
import com.dth.fashionshop.modules.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);
}