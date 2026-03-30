package com.dth.fashionshop.modules.identity.repository;

import com.dth.fashionshop.modules.identity.entity.InvalidatedToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {

    @Transactional
    void deleteByExpiryTimeBefore(Date now);
}