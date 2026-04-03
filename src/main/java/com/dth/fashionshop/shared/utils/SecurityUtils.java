package com.dth.fashionshop.shared.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    // Hàm lấy email người dùng đang đăng nhập
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null &&
                authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {

            return authentication.getName();
        }

        throw new RuntimeException("Người dùng chưa đăng nhập hoặc phiên làm việc đã hết hạn!");
    }
}