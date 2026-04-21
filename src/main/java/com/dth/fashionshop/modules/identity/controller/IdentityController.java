package com.dth.fashionshop.modules.identity.controller;

import com.dth.fashionshop.modules.identity.dto.request.*;
import com.dth.fashionshop.modules.identity.dto.response.LoginResponse;
import com.dth.fashionshop.modules.identity.dto.response.VerifyResetOtpResponse;
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

    // Đăng ký và gửi mã OTP
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request){
        identityService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Đăng ký thành công! Vui lòng kiểm tra email để lấy mã OTP."));
    }

    // Xác thực OTP đăng ký và kích hoạt tài khoản
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request){
        identityService.verifyOtp(request);

        return ResponseEntity.ok(Map.of("message", "Xác thực OTP thành công! Tài khoản đã được kích hoạt."));
    }

    // Yêu cầu cấp lại OTP xác thực đăng ký
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        identityService.resendOtp(request);
        return ResponseEntity.ok(Map.of("message", "Mã OTP mới đã được gửi! Vui lòng kiểm tra email."));
    }

    // Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        LoginResponse response = identityService.login(request);

        return ResponseEntity.ok(response);
    }

    // Đăng xuất
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            identityService.logout(token);
            return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công!"));
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy Token hợp lệ để đăng xuất!"));
    }

    // Quên mật khẩu và yêu cầu cấp OTP xác thực để khôi phục mật khẩu mới (dùng cho cả yêu cầu cấp lại OTP)
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        identityService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "Mã OTP khôi phục mật khẩu đã được gửi đến email của bạn!"));
    }

    // Xác thực OTP quên mật khẩu
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<VerifyResetOtpResponse> verifyResetOtp(@Valid @RequestBody VerifyResetOtpRequest request) {
        VerifyResetOtpResponse response = identityService.verifyResetOtp(request);
        return ResponseEntity.ok(response);
    }

    // Đổi mật khẩu sau khi đã xác thực OTP quên mật khẩu
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody ResetPasswordRequest request) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu mã xác thực (Reset Token)!"));
        }

        String token = authHeader.substring(7);
        identityService.resetPassword(token, request);

        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại với mật khẩu mới."));
    }
}