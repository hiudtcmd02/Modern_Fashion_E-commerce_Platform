package com.dth.fashionshop.modules.catalog.controller;

import com.dth.fashionshop.modules.catalog.dto.request.ProductFilterRequest;
import com.dth.fashionshop.modules.catalog.dto.request.ProductRequest;
import com.dth.fashionshop.modules.catalog.dto.response.ProductDetailResponse;
import com.dth.fashionshop.modules.catalog.dto.response.ProductListAdminResponse;
import com.dth.fashionshop.modules.catalog.dto.response.ProductStatsResponse;
import com.dth.fashionshop.modules.catalog.dto.response.ProductSuggestionResponse;
import com.dth.fashionshop.modules.catalog.service.ProductService;
import com.dth.fashionshop.shared.utils.PaginationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminProductController {

    private final ProductService productService;

    // Lấy danh sách cơ bản (Không dùng cho giao diện)
    @GetMapping
    public ResponseEntity<Page<ProductDetailResponse>> getBasicProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        int pageNumber = PaginationUtils.correctPageNo(page);

        return ResponseEntity.ok(productService.getBasicProducts(pageNumber, size));
    }

    // Xem chi tiết sản phẩm
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // Thêm mới sản phẩm
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDetailResponse> createProduct(
            @RequestPart("product") @Valid ProductRequest request,
            @RequestPart("thumbnail") MultipartFile thumbnail,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request, thumbnail, images));
    }

    // Cập nhật sản phẩm
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDetailResponse> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") @Valid ProductRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ResponseEntity.ok(productService.updateProduct(id, request, thumbnail, images));
    }

    // Xóa mềm sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Xóa sản phẩm thành công!"));
    }

    // Khôi phục sản phẩm
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ProductDetailResponse> restoreProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.restoreProduct(id));
    }

    // Xóa riêng lẻ 1 ảnh phụ
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<?> deleteProductImage(@PathVariable Long imageId) {
        productService.deleteProductImage(imageId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa hình ảnh thành công!"));
    }

    // Thống kê nhanh
    @GetMapping("/stats")
    public ResponseEntity<ProductStatsResponse> getProductStats() {
        return ResponseEntity.ok(productService.getProductStats());
    }

    // Tìm kiếm và lọc đa luồng (Admin list products)
    @GetMapping("/search")
    public ResponseEntity<Page<ProductListAdminResponse>> searchAndFilterAdminProducts(
            @ModelAttribute ProductFilterRequest filter,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        int pageNumber = PaginationUtils.correctPageNo(page);

        return ResponseEntity.ok(productService.searchAndFilterAdminProducts(filter, pageNumber, size));
    }

    // Gợi ý tìm kiếm
    @GetMapping("/suggestions")
    public ResponseEntity<List<ProductSuggestionResponse>> getProductSuggestions(
            @RequestParam String keyword) {
        return ResponseEntity.ok(productService.getAdminProductSuggestions(keyword));
    }
}