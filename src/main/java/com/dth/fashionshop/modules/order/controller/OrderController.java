package com.dth.fashionshop.modules.order.controller;

import com.dth.fashionshop.modules.order.dto.request.CheckoutPreviewRequest;
import com.dth.fashionshop.modules.order.dto.request.CreateOrderRequest;
import com.dth.fashionshop.modules.order.dto.response.CheckoutPreviewResponse;
import com.dth.fashionshop.modules.order.dto.response.OrderResponse;
import com.dth.fashionshop.modules.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Xem trước thông tin đặt hàng
    @PostMapping("/preview")
    public ResponseEntity<CheckoutPreviewResponse> previewOrder(
            @Valid @RequestBody CheckoutPreviewRequest request) {

        return ResponseEntity.ok(orderService.previewOrder(request));
    }

    // Đặt hàng và tạo đơn hàng
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }
}