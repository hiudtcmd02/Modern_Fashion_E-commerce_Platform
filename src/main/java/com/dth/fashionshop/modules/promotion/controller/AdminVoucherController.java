package com.dth.fashionshop.modules.promotion.controller;

import com.dth.fashionshop.modules.promotion.dto.request.VoucherRequest;
import com.dth.fashionshop.modules.promotion.dto.response.VoucherResponse;
import com.dth.fashionshop.modules.promotion.enums.VoucherTab;
import com.dth.fashionshop.modules.promotion.service.VoucherService;
import com.dth.fashionshop.shared.utils.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin quản lý Voucher khuyến mãi", description = "Các API giúp Admin thực hiện quản lý mã giảm giá")
public class AdminVoucherController {

    private final VoucherService voucherService;

    @Operation(summary = "Lấy danh sách Voucher", description = "Tìm kiếm theo keyword (Mã/Tên), lọc theo tab trạng thái (ALL, UPCOMING, ONGOING, ENDED)")
    @GetMapping
    public ResponseEntity<Page<VoucherResponse>> getAllVouchers(
            @Parameter(description = "Từ khóa tìm kiếm (Mã Voucher hoặc Tên Voucher)")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Lọc theo trạng thái: ALL (Tất cả), UPCOMING (Sắp diễn ra), ONGOING (Đang diễn ra), ENDED (Đã kết thúc)")
            @RequestParam(defaultValue = "ALL") VoucherTab tab,

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        int pageNumber = PaginationUtils.correctPageNo(page);
        return ResponseEntity.ok(voucherService.getAllVouchers(keyword, tab, pageNumber, size));
    }

    @Operation(summary = "Lấy thông tin chi tiết một Voucher")
    @GetMapping("/{id}")
    public ResponseEntity<VoucherResponse> getVoucherById(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.getVoucherById(id));
    }

    @Operation(summary = "Tạo mới Voucher", description = "Lưu ý: Nếu Voucher không giới hạn số lượt (is_unlimited = true), FE phải truyền usage_limit = null")
    @PostMapping
    public ResponseEntity<VoucherResponse> createVoucher(@Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(voucherService.createVoucher(request));
    }

    @Operation(summary = "Cập nhật Voucher", description = "Lưu ý: Nếu Voucher không giới hạn số lượt (is_unlimited = true), FE phải truyền usage_limit = null")
    @PutMapping("/{id}")
    public ResponseEntity<VoucherResponse> updateVoucher(
            @PathVariable Long id,
            @Valid @RequestBody VoucherRequest request) {

        return ResponseEntity.ok(voucherService.updateVoucher(id, request));
    }

    @Operation(summary = "Kết thúc sớm Voucher", description = "Kết thúc sớm Voucher dù chưa đến thời gian kết thúc")
    @PatchMapping("/{id}/end-early")
    public ResponseEntity<?> endVoucherEarly(@PathVariable Long id) {
        voucherService.endVoucherEarly(id);
        return ResponseEntity.ok(Map.of("message", "Đã kết thúc sớm Voucher thành công!"));
    }
}