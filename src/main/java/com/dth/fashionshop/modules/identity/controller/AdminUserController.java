package com.dth.fashionshop.modules.identity.controller;

import com.dth.fashionshop.modules.identity.dto.response.UserAdminResponse;
import com.dth.fashionshop.modules.identity.dto.response.UserDetailAdminResponse;
import com.dth.fashionshop.modules.identity.enums.UserStatus;
import com.dth.fashionshop.modules.identity.service.AdminUserService;
import com.dth.fashionshop.shared.utils.PaginationUtils;
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

    // Lấy danh sách người dùng (Tìm kiếm, Lọc, Phân trang)
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<UserAdminResponse>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        int pageNumber = PaginationUtils.correctPageNo(page);

        return ResponseEntity.ok(adminUserService.getAllUsers(keyword, status, pageNumber, size));
    }

    // Khóa / Mở khóa tài khoản
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Long id) {

        adminUserService.toggleUserStatus(id);

        return ResponseEntity.ok("Cập nhật trạng thái tài khoản thành công!");
    }

    // Lấy thông tin chi tiết người dùng (Customer 360 View)
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDetailAdminResponse> getUserDetail(@PathVariable Long id) {

        return ResponseEntity.ok(adminUserService.getUserDetailById(id));
    }
}