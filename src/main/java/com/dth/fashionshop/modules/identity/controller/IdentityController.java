package com.dth.fashionshop.modules.identity.controller;

import com.dth.fashionshop.modules.identity.dto.request.RegisterRequest;
import com.dth.fashionshop.modules.identity.dto.request.VerifyOtpRequest;
import com.dth.fashionshop.modules.identity.service.IdentityService;
import jakarta.validation.Valid;
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
}