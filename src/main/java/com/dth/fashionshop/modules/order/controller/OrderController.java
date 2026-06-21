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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Khách hàng đặt hàng và quản lý đơn hàng cá nhân", description = "Các API dành cho Khách hàng đặt hàng và quản lý đơn hàng cá nhân")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Xem trước thông tin đặt hàng",
            description = "Lấy dữ liệu hiển thị lên giao diện checkout để Khách hàng có thể kiểm tra và điều chỉnh thông tin đơn hàng trước khi đặt hàng")
    @PostMapping("/preview")
    public ResponseEntity<CheckoutPreviewResponse> previewOrder(
            @Valid @RequestBody CheckoutPreviewRequest request) {

        return ResponseEntity.ok(orderService.previewOrder(request));
    }

    @Operation(summary = "Khách hàng đặt hàng và tạo đơn hàng")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody CreateOrderRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request, httpServletRequest));
    }

    @Operation(summary = "Lấy thông tin đơn hàng để hiển thị/ reload trang thông báo đặt hàng thành công (Success)")
    @GetMapping("/success-info/{orderCode}")
    public ResponseEntity<OrderResponse> getOrderSuccessInfo(
            @Parameter(description = "Mã đơn hàng", example = "ORD-1776702051867-6")
            @PathVariable String orderCode)
    {
        return ResponseEntity.ok(orderService.getOrderSuccessInfo(orderCode));
    }

    @Operation(summary = "Lấy danh sách đơn hàng",
            description = "Hỗ trợ Khách hàng lọc danh sách đơn hàng cá nhân theo tab trạng thái và phân trang")
    @GetMapping("/my-orders")
    public ResponseEntity<Page<OrderListResponse>> getMyOrders(
            @Parameter(description = "Lọc theo tab trạng thái: " +
                    "ALL - Tất cả, " +
                    "WAITING_PAYMENT - Đang chờ thanh toán, " +
                    "PENDING - Đang chờ xử lý, " +
                    "PROCESSING - Đang chuẩn bị đơn hàng, " +
                    "SHIPPING - Đang giao, " +
                    "COMPLETED - Hoàn thành, " +
                    "CANCELLED - Đã hủy, " +
                    "RETURNED - Đã trả hàng.")
            @RequestParam(defaultValue = "ALL") OrderTab tab,

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        int pageNumber = PaginationUtils.correctPageNo(page);
        return ResponseEntity.ok(orderService.getMyOrders(tab, pageNumber, size));
    }

    @Operation(summary = "Lấy thông tin chi tiết của một đơn hàng")
    @GetMapping("/{orderCode}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(
            @Parameter(description = "Mã đơn hàng", example = "ORD-1776702051867-6")
            @PathVariable String orderCode)
    {
        return ResponseEntity.ok(orderService.getOrderDetail(orderCode));
    }

    @Operation(summary = "Hủy đơn hàng",
            description = "Cho phép Khách hàng hủy đơn hàng có trạng thái là PENDING hoặc PROCESSING và chưa được thanh toán")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(Map.of("message", "Hủy đơn hàng thành công!"));
    }
}