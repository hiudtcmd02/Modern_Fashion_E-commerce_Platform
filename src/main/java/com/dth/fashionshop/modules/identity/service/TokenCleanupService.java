package com.dth.fashionshop.modules.identity.service;

import com.dth.fashionshop.modules.identity.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredTokens() {
        log.info("[CRON JOB] Bắt đầu dọn dẹp các Token hết hạn trong Blacklist...");
        invalidatedTokenRepository.deleteByExpiryTimeBefore(new Date());
        log.info("[CRON JOB] Dọn dẹp hoàn tất!");
    }
}