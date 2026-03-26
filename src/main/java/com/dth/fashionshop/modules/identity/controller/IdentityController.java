package com.dth.fashionshop.modules.identity.controller;

import com.dth.fashionshop.modules.identity.dto.request.LoginRequest;
import com.dth.fashionshop.modules.identity.dto.request.RegisterRequest;
import com.dth.fashionshop.modules.identity.dto.request.ResendOtpRequest;
import com.dth.fashionshop.modules.identity.dto.request.VerifyOtpRequest;
import com.dth.fashionshop.modules.identity.dto.response.LoginResponse;
import com.dth.fashionshop.modules.identity.service.IdentityService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/identity")
@RequiredArgsConstructor
public class IdentityController {

    private final IdentityService identityService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request){
        identityService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Đăng ký thành công! Vui lòng kiểm tra email để lấy mã OTP."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request){
        identityService.verifyOtp(request);

        return ResponseEntity.ok(Map.of("message", "Xác thực OTP thành công! Tài khoản đã được kích hoạt."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        identityService.resendOtp(request);
        return ResponseEntity.ok(Map.of("message", "Mã OTP mới đã được gửi! Vui lòng kiểm tra email."));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // Gọi xuống Service để xử lý và nhận về Hộp kết quả (chứa Token)
        LoginResponse response = identityService.login(request);

        // Trả về cho Frontend với mã 200 OK
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        // Rút thẻ từ Header ra
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Cắt bỏ chữ Bearer
            identityService.logout(token);
            return ResponseEntity.ok("Đăng xuất thành công!");
        }

        return ResponseEntity.badRequest().body("Không tìm thấy Token hợp lệ để đăng xuất!");
    }
}