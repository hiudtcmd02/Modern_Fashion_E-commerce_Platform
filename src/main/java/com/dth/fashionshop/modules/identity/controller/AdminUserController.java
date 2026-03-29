package com.dth.fashionshop.modules.identity.controller;

import com.dth.fashionshop.modules.identity.dto.response.UserAdminResponse;
import com.dth.fashionshop.modules.identity.enums.UserStatus;
import com.dth.fashionshop.modules.identity.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    // 1. Lấy danh sách người dùng (Tìm kiếm, Lọc, Phân trang)
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // 🛡️ KHIÊN BẢO VỆ: Chỉ có Role ADMIN mới được gọi API này
    public ResponseEntity<Page<UserAdminResponse>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "1") int page, // Frontend thường truyền trang 1, 2, 3...
            @RequestParam(defaultValue = "10") int size // Số dòng trên 1 trang
    ) {

        // Frontend đếm trang từ 1, nhưng Spring Boot (JPA) đếm từ 0.
        // Cú chuyển đổi này giúp cả 2 bên nói chung một ngôn ngữ.
        int pageNumber = page > 0 ? page - 1 : 0;

        return ResponseEntity.ok(adminUserService.getAllUsers(keyword, status, pageNumber, size));
    }
}