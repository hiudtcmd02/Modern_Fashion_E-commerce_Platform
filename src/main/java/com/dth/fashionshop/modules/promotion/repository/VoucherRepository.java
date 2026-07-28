package com.dth.fashionshop.modules.promotion.repository;

import com.dth.fashionshop.modules.promotion.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    // Tìm kiếm (theo mã, tên) và lọc theo trạng thái
    @Query("SELECT v FROM Voucher v WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(v.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (" +
            "  :status IS NULL OR :status = '' OR :status = 'ALL' " +
            "  OR (:status = 'UPCOMING' AND v.isActive = true AND v.startDate > :now) " +
            "  OR (:status = 'ONGOING' AND v.isActive = true AND v.startDate <= :now AND v.endDate >= :now) " +
            "  OR (:status = 'ENDED' AND (v.isActive = false OR v.endDate < :now))" +
            ")")
    Page<Voucher> searchAndFilterAdminVouchers(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("now") LocalDateTime now,
            Pageable pageable);
}