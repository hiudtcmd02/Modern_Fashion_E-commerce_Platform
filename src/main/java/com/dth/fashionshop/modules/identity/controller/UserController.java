package com.dth.fashionshop.modules.identity.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    //API này hiện chỉ để test "thử thẻ" kiểm tra hệ thống đã có thể nhận diện ai đang đăng nhập thành công chưa?
    @GetMapping("/profile")
    public ResponseEntity<String> getMyProfile() {
        // Gọi thẳng lên "Loa phát thanh" của Tòa nhà để hỏi xem ai đang đi lại trong này
        // Không cần truyền Token hay Email gì vào tham số của hàm này cả!
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        String thongBao = "Xin chào! Máy quét thẻ hoạt động hoàn hảo. Căn cước của bạn là: " + email;

        return ResponseEntity.ok(thongBao);
    }
}