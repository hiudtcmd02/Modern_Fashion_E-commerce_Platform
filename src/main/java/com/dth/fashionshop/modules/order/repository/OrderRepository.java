package com.dth.fashionshop.modules.order.repository;

import com.dth.fashionshop.modules.identity.entity.User;
import com.dth.fashionshop.modules.order.entity.Order;
import com.dth.fashionshop.modules.order.enums.OrderStatus;
import com.dth.fashionshop.modules.order.enums.PaymentMethod;
import com.dth.fashionshop.modules.order.enums.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUser(User user, Pageable pageable);

    Page<Order> findByUserAndOrderStatus(User user, OrderStatus orderStatus, Pageable pageable);

    Page<Order> findByUserAndOrderStatusAndPaymentStatusAndPaymentMethod(
            User user,
            OrderStatus orderStatus,
            PaymentStatus paymentStatus,
            PaymentMethod paymentMethod,
            Pageable pageable);

    Optional<Order> findByOrderCodeAndUser(String orderCode, User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.user = :user")
    Optional<Order> findByIdAndUserWithLock(@Param("id") Long id, @Param("user") User user);
}