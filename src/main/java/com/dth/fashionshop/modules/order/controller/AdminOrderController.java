package com.dth.fashionshop.modules.order.controller;

import com.dth.fashionshop.modules.order.dto.request.UpdateOrderStatusRequest;
import com.dth.fashionshop.modules.order.dto.response.AdminOrderDetailResponse;
import com.dth.fashionshop.modules.order.dto.response.AdminOrderListResponse;
import com.dth.fashionshop.modules.order.enums.OrderTab;
import com.dth.fashionshop.modules.order.service.AdminOrderService;
import com.dth.fashionshop.shared.utils.PaginationUtils;
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
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    // Lấy danh sách đơn hàng (Tìm kiếm, lọc, phân trang)
    @GetMapping
    public ResponseEntity<Page<AdminOrderListResponse>> getAllOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "ALL") OrderTab tab,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        int pageNumber = PaginationUtils.correctPageNo(page);
        return ResponseEntity.ok(adminOrderService.getAllOrdersForAdmin(keyword, tab, pageNumber, size));
    }

    // Lấy thông tin chi tiết đơn hàng
    @GetMapping("/{id}")
    public ResponseEntity<AdminOrderDetailResponse> getOrderDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminOrderService.getOrderDetailForAdmin(id));
    }

    // Cập nhật trạng thái và ghi chú nội bộ của đơn hàng
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        adminOrderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(Map.of("message", "Cập nhật đơn hàng thành công!"));
    }
}