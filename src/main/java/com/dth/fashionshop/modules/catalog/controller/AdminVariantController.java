package com.dth.fashionshop.modules.catalog.controller;

import com.dth.fashionshop.modules.catalog.dto.request.VariantInlineRequest;
import com.dth.fashionshop.modules.catalog.dto.response.VariantResponse;
import com.dth.fashionshop.modules.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin xem và cập nhật nhanh phân loại (biến thể) sản phẩm",
        description = "Các API dành cho Admin thực hiện xem, cập nhật tồn kho và đổi trạng thái hoạt động của phân loại (biến thể) nhanh chóng " +
                "ngay trên giao diện danh sách sản phẩm của Admin")
public class AdminVariantController {

    private final ProductService productService;

    @Operation(summary = "Lấy danh sách phân loại (biến thể) của sản phẩm khi ở trang danh sách sản phẩm của Admin")
    @GetMapping("/products/{productId}/variants")
    public ResponseEntity<List<VariantResponse>> getVariantsByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getVariantsByProductId(productId));
    }

    @Operation(summary = "Cập nhật tồn kho nhanh cho phân loại (biến thể) ngay trên giao diện danh sách sản phẩm của Admin")
    @PatchMapping("/variants/{id}/stock")
    public ResponseEntity<VariantResponse> updateVariantStock(
            @PathVariable Long id,
            @RequestBody VariantInlineRequest request) {
        return ResponseEntity.ok(productService.updateVariantStock(id, request.getStockQuantity()));
    }

    @Operation(summary = "Thay đổi nhanh trạng thái cho phân loại (biến thể) ngay trên giao diện danh sách sản phẩm của Admin")
    @PatchMapping("/variants/{id}/status")
    public ResponseEntity<VariantResponse> toggleVariantStatus(
            @PathVariable Long id,
            @RequestBody VariantInlineRequest request) {
        return ResponseEntity.ok(productService.toggleVariantStatus(id, request.getIsActive()));
    }
}