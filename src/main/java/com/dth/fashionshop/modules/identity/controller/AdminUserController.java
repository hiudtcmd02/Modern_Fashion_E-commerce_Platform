package com.dth.fashionshop.modules.identity.controller;

import com.dth.fashionshop.modules.identity.dto.response.UserAdminResponse;
import com.dth.fashionshop.modules.identity.dto.response.UserDetailAdminResponse;
import com.dth.fashionshop.modules.identity.enums.UserStatus;
import com.dth.fashionshop.modules.identity.service.AdminUserService;
import com.dth.fashionshop.shared.utils.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin quản lý người dùng", description = "Các API dành cho Admin tra cứu thông tin người dùng và khóa / mở khóa tài khoản người dùng")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "Lấy danh sách người dùng",
            description = "Hỗ trợ tìm kiếm theo keyword, lọc theo trạng thái và phân trang")
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<UserAdminResponse>> getAllUsers(
            @Parameter(description = "Tìm kiếm theo họ tên, email và số điện thoại của người dùng")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Lọc theo trạng thái của tài khoản: INACTIVE, ACTIVE, LOCKED và tất cả trạng thái")
            @RequestParam(required = false) UserStatus status,

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        int pageNumber = PaginationUtils.correctPageNo(page);

        return ResponseEntity.ok(adminUserService.getAllUsers(keyword, status, pageNumber, size));
    }

    @Operation(summary = "Khóa/ Mở khóa tài khoản",
            description = "Hỗ trợ khóa tài khoản đang ACTIVE và mở khóa tài khoản đang LOCKED")
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {

        adminUserService.toggleUserStatus(id);

        return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái tài khoản thành công!"));
    }

    @Operation(summary = "Lấy thông tin chi tiết người dùng")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDetailAdminResponse> getUserDetail(@PathVariable Long id) {

        return ResponseEntity.ok(adminUserService.getUserDetailById(id));
    }
}