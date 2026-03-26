package com.dth.fashionshop.modules.identity.repository;

import com.dth.fashionshop.modules.identity.entity.InvalidatedToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    // Tìm và Xóa tất cả các Token có expiryTime nhỏ hơn (Before) thời gian hiện tại
    @Transactional
    // BẮT BUỘC PHẢI CÓ KHI THỰC HIỆN XÓA/SỬA
    void deleteByExpiryTimeBefore(Date now);
}