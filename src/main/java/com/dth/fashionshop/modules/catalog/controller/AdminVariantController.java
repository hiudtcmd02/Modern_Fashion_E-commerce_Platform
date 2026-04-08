package com.dth.fashionshop.modules.catalog.controller;

import com.dth.fashionshop.modules.catalog.dto.request.VariantInlineRequest;
import com.dth.fashionshop.modules.catalog.dto.response.VariantResponse;
import com.dth.fashionshop.modules.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminVariantController {

    private final ProductService productService;

    // Lấy danh sách phân loại (biến thể) khi bấm nút [+] trên sản phẩm khi ở trang danh sách sản phẩm của admin
    @GetMapping("/products/{productId}/variants")
    public ResponseEntity<List<VariantResponse>> getVariantsByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getVariantsByProductId(productId));
    }

    // Cập nhật tồn kho nhanh cho phân loại (biến thể) ngay trên giao diện danh sách sản phẩm của admin
    @PatchMapping("/variants/{id}/stock")
    public ResponseEntity<VariantResponse> updateVariantStock(
            @PathVariable Long id,
            @RequestBody VariantInlineRequest request) {
        return ResponseEntity.ok(productService.updateVariantStock(id, request.getStockQuantity()));
    }

    // Bật/Tắt nhanh trạng thái cho phân loại (biến thể) ngay trên giao diện danh sách sản phẩm của admin
    @PatchMapping("/variants/{id}/status")
    public ResponseEntity<VariantResponse> toggleVariantStatus(
            @PathVariable Long id,
            @RequestBody VariantInlineRequest request) {
        return ResponseEntity.ok(productService.toggleVariantStatus(id, request.getIsActive()));
    }
}