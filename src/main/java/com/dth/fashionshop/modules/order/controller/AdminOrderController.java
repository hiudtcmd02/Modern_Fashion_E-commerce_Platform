package com.dth.fashionshop.modules.order.controller;

import com.dth.fashionshop.modules.order.dto.request.UpdateOrderStatusRequest;
import com.dth.fashionshop.modules.order.dto.response.AdminOrderDetailResponse;
import com.dth.fashionshop.modules.order.dto.response.AdminOrderListResponse;
import com.dth.fashionshop.modules.order.enums.OrderTab;
import com.dth.fashionshop.modules.order.service.AdminOrderService;
import com.dth.fashionshop.shared.utils.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin quản lý và cập nhật trạng thái đơn hàng", description = "Các API dành cho Admin tra cứu và cập nhật đơn hàng")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @Operation(summary = "Lấy danh sách đơn hàng", description = "Hỗ trợ tìm kiếm theo keyword, lọc theo tab trạng thái và phân trang")
    @GetMapping
    public ResponseEntity<Page<AdminOrderListResponse>> getAllOrders(
            @Parameter(description = "Tìm theo mã đơn hàng, tên hoặc SĐT ( Người đặt/ Người nhận)")
            @RequestParam(required = false) String keyword,

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
        return ResponseEntity.ok(adminOrderService.getAllOrdersForAdmin(keyword, tab, pageNumber, size));
    }

    @Operation(summary = "Lấy thông tin chi tiết đơn hàng")
    @GetMapping("/{id}")
    public ResponseEntity<AdminOrderDetailResponse> getOrderDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminOrderService.getOrderDetailForAdmin(id));
    }

    @Operation(summary = "Cập nhật trạng thái và ghi chú nội bộ của đơn hàng")
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        adminOrderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(Map.of("message", "Cập nhật đơn hàng thành công!"));
    }
}