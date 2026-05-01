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

    // Lấy đơn hàng kèm khóa bi quan phía khách hàng
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.user = :user")
    Optional<Order> findByIdAndUserWithLock(@Param("id") Long id, @Param("user") User user);

    // Tìm kiếm tổng hợp cho các Tab thông thường
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user u WHERE " +
            "(:keyword IS NULL OR :keyword = '' " +
            "OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(o.receiverName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR o.receiverPhone LIKE CONCAT('%', :keyword, '%') " +
            "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR u.phoneNumber LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:status IS NULL OR o.orderStatus = :status)")
    Page<Order> searchAdminOrders(@Param("keyword") String keyword, @Param("status") OrderStatus status, Pageable pageable);

    // Tìm kiếm tổng hợp cho riêng Tab "Đang chờ thanh toán" (WAITING_PAYMENT)
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user u WHERE " +
            "(:keyword IS NULL OR :keyword = '' " +
            "OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(o.receiverName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR o.receiverPhone LIKE CONCAT('%', :keyword, '%') " +
            "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR u.phoneNumber LIKE CONCAT('%', :keyword, '%')) " +
            "AND (o.orderStatus = 'PENDING' AND o.paymentStatus = 'UNPAID' AND o.paymentMethod = 'VNPAY')")
    Page<Order> searchAdminWaitingPaymentOrders(@Param("keyword") String keyword, Pageable pageable);

    // Lấy đơn hàng kèm khóa bi quan phía Admin
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithLockForAdmin(@Param("id") Long id);
}