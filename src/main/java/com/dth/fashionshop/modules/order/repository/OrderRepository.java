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

import java.time.LocalDateTime;
import java.util.List;

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

    // Truy vấn tìm các đơn hàng VNPAY bị treo quá thời gian quy định
    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'PENDING' " +
            "AND o.paymentStatus = 'UNPAID' " +
            "AND o.paymentMethod = 'VNPAY' " +
            "AND o.createdAt < :cutoffTime")
    List<Order> findExpiredVnpayOrders(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Truy vấn lấy danh sách ID của các đơn hàng VNPAY bị treo quá thời gian quy định
    @Query("SELECT o.id FROM Order o WHERE o.orderStatus = 'PENDING' " +
            "AND o.paymentStatus = 'UNPAID' " +
            "AND o.paymentMethod = 'VNPAY' " +
            "AND o.createdAt < :cutoffTime")
    List<Long> findExpiredVnpayOrderIds(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Hàm tính tổng doanh thu
    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.orderStatus = 'COMPLETED' AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Long calculateTotalRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Hàm đếm tổng đơn hoàn thành
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = 'COMPLETED' AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Long countCompletedOrders(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Hàm thống kê doanh thu theo ngày
    @Query(value = "SELECT DATE_FORMAT(created_at, '%d/%m/%Y') as label, SUM(final_amount) as value " +
            "FROM orders WHERE order_status = 'COMPLETED' " +
            "AND created_at >= :startDate AND created_at <= :endDate " +
            "GROUP BY label, DATE(created_at) " +
            "ORDER BY DATE(created_at) ASC", nativeQuery = true)
    List<Object[]> getRevenueByDay(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Hàm thống kê doanh thu theo tháng
    @Query(value = "SELECT DATE_FORMAT(created_at, '%m/%Y') as label, SUM(final_amount) as value " +
            "FROM orders WHERE order_status = 'COMPLETED' " +
            "AND created_at >= :startDate AND created_at <= :endDate " +
            "GROUP BY label, DATE_FORMAT(created_at, '%Y-%m') " +
            "ORDER BY DATE_FORMAT(created_at, '%Y-%m') ASC", nativeQuery = true)
    List<Object[]> getRevenueByMonth(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}