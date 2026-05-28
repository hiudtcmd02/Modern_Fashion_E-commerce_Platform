package com.dth.fashionshop.modules.identity.controller;

import com.dth.fashionshop.modules.identity.dto.request.ChangePasswordRequest;
import com.dth.fashionshop.modules.identity.dto.request.UpdateProfileRequest;
import com.dth.fashionshop.modules.identity.dto.response.UserProfileResponse;
import com.dth.fashionshop.modules.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Khách hàng quản lý thông tin cá nhân", description = "Các API phục vụ cho việc quản lý thông tin cá nhân của khách hàng và đổi mật khẩu")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Khách hàng lấy thông tin cá nhân")
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @Operation(summary = "Cập nhật thông tin chữ (Text) của khách hàng",
            description = "API này dùng để cập nhật các thông tin như họ tên, số điện thoại, giới tính, ngày sinh của khách hàng")
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @Operation(summary = "Tải lên và cập nhật ảnh đại diện")
    @PostMapping("/profile/avatar")
    public ResponseEntity<UserProfileResponse> uploadAvatar(
            @Parameter(description = "Ảnh đại diện")
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.uploadAvatar(file));
    }

    @Operation(summary = "Khách hàng đổi mật khẩu trong hồ sơ cá nhân")
    @PutMapping("/profile/password")
    public ResponseEntity<?> changePassword(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody ChangePasswordRequest request) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu mã xác thực!"));
        }

        String token = authHeader.substring(7);
        userService.changePassword(token, request);

        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công. Vui lòng đăng nhập lại!"));
    }
}