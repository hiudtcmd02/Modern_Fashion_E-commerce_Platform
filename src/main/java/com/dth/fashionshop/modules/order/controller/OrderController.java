package com.dth.fashionshop.modules.order.controller;

import com.dth.fashionshop.modules.order.dto.request.CheckoutPreviewRequest;
import com.dth.fashionshop.modules.order.dto.request.CreateOrderRequest;
import com.dth.fashionshop.modules.order.dto.response.CheckoutPreviewResponse;
import com.dth.fashionshop.modules.order.dto.response.OrderDetailResponse;
import com.dth.fashionshop.modules.order.dto.response.OrderListResponse;
import com.dth.fashionshop.modules.order.dto.response.OrderResponse;
import com.dth.fashionshop.modules.order.enums.OrderTab;
import com.dth.fashionshop.modules.order.service.OrderService;
import com.dth.fashionshop.shared.utils.PaginationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    // Lấy thông tin reload trang Success
    @GetMapping("/success-info/{orderCode}")
    public ResponseEntity<OrderResponse> getOrderSuccessInfo(@PathVariable String orderCode) {
        return ResponseEntity.ok(orderService.getOrderSuccessInfo(orderCode));
    }

    // Lấy danh sách đơn hàng cho khách hàng
    @GetMapping("/my-orders")
    public ResponseEntity<Page<OrderListResponse>> getMyOrders(
            @RequestParam(defaultValue = "ALL") OrderTab tab,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        int pageNumber = PaginationUtils.correctPageNo(page);
        return ResponseEntity.ok(orderService.getMyOrders(tab, pageNumber, size));
    }

    // Lấy thông tin chi tiết của một đơn hàng
    @GetMapping("/{orderCode}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable String orderCode) {
        return ResponseEntity.ok(orderService.getOrderDetail(orderCode));
    }

    // Khách hàng tự hủy đơn hàng
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(Map.of("message", "Hủy đơn hàng thành công!"));
    }
}