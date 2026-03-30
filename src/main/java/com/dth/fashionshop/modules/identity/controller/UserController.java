package com.dth.fashionshop.modules.identity.controller;

import com.dth.fashionshop.modules.identity.dto.request.ChangePasswordRequest;
import com.dth.fashionshop.modules.identity.dto.request.UpdateProfileRequest;
import com.dth.fashionshop.modules.identity.dto.response.UserProfileResponse;
import com.dth.fashionshop.modules.identity.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Lấy thông tin cá nhân
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    // Cập nhật thông tin chữ (Text)
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    // Tải lên ảnh đại diện
    @PostMapping("/profile/avatar")
    public ResponseEntity<UserProfileResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.uploadAvatar(file));
    }

    // Người dùng đổi mật khẩu trong hồ sơ cá nhân
    @PutMapping("/profile/password")
    public ResponseEntity<String> changePassword(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody ChangePasswordRequest request) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Thiếu mã xác thực!");
        }

        String token = authHeader.substring(7);
        userService.changePassword(token, request);

        return ResponseEntity.ok("Đổi mật khẩu thành công. Vui lòng đăng nhập lại!");
    }
}