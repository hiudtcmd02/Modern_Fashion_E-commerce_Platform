package com.dth.fashionshop.modules.promotion.controller;

import com.dth.fashionshop.modules.promotion.dto.request.VoucherApplyRequest;
import com.dth.fashionshop.modules.promotion.dto.response.VoucherApplyResponse;
import com.dth.fashionshop.modules.promotion.dto.response.VoucherGuestResponse;
import com.dth.fashionshop.modules.promotion.enums.VoucherTab;
import com.dth.fashionshop.modules.promotion.service.VoucherService;
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

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
@Tag(name = "Khách hàng tra cứu và áp dụng Voucher", description = "Các API phục vụ hiển thị Kho voucher và Áp dụng mã lúc thanh toán")
public class StorefrontVoucherController {

    private final VoucherService voucherService;

    @Operation(summary = "Lấy danh sách Voucher", description = "Lấy các Voucher Đang diễn ra hoặc Sắp diễn ra. Cho phép tìm kiếm và lọc theo tab trạng thái")
    @GetMapping("/active")
    public ResponseEntity<Page<VoucherGuestResponse>> getActiveVouchers(
            @Parameter(description = "Từ khóa tìm kiếm (Mã hoặc Tên Voucher)")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Lọc theo trạng thái: ALL, UPCOMING, ONGOING")
            @RequestParam(defaultValue = "ALL") VoucherTab tab,

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        int pageNumber = PaginationUtils.correctPageNo(page);
        return ResponseEntity.ok(voucherService.getStorefrontVouchers(keyword, tab, pageNumber, size));
    }

    @Operation(summary = "Khách hàng xem thông tin chi tiết của Voucher")
    @GetMapping("/{code}")
    public ResponseEntity<VoucherGuestResponse> getVoucherDetail(@PathVariable String code) {
        return ResponseEntity.ok(voucherService.getVoucherDetailByCode(code));
    }

    @Operation(summary = "Tính toán số tiền giảm giá", description = "Tính số tiền được giảm của Voucher. Dùng ở màn hình Checkout khi Khách hàng bấm Áp dụng Voucher")
    @PostMapping("/apply")
    public ResponseEntity<VoucherApplyResponse> applyVoucher(
            @Valid @RequestBody VoucherApplyRequest request) {

        return ResponseEntity.ok(voucherService.calculateDiscount(request));
    }
}