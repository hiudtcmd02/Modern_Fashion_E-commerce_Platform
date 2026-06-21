package com.dth.fashionshop.modules.order.repository;

import com.dth.fashionshop.modules.order.entity.PaymentTransaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.id = :id")
    Optional<PaymentTransaction> findByIdForUpdate(@Param("id") Long id);

}