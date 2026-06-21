package com.dth.fashionshop.modules.order.controller;

import com.dth.fashionshop.modules.order.dto.response.IpnResponse;
import com.dth.fashionshop.modules.order.dto.response.PaymentResultResponse;
import com.dth.fashionshop.modules.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Khách hàng thanh toán giao dịch trực tuyến",
        description = "Các API phục vụ IPN, Webhook và xử lý trạng thái đơn hàng thanh toán bằng VNPAY")
public class PaymentController {

    private final OrderService orderService;

    @Operation(summary = "Frontend gọi để xác nhận và hiển thị kết quả giao dịch")
    @GetMapping("/vnpay-return")
    public ResponseEntity<PaymentResultResponse> processReturnUrl(HttpServletRequest request) {
        return ResponseEntity.ok(orderService.processReturnUrl(request));
    }

    @Operation(summary = "Webhook IPN do VNPAY gọi ngầm")
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<IpnResponse> processIpn(HttpServletRequest request) {
        return ResponseEntity.ok(orderService.processIpn(request));
    }

    @Operation(summary = "Khởi tạo lại link thanh toán (Thanh toán lại)")
    @PostMapping("/retry")
    public ResponseEntity<String> retryPayment(
            @Parameter(description = "Mã đơn hàng", example = "ORD-1776702051867-6")
            @RequestParam String orderCode,

            HttpServletRequest request)
    {
        return ResponseEntity.ok(orderService.retryPayment(orderCode, request));
    }
}