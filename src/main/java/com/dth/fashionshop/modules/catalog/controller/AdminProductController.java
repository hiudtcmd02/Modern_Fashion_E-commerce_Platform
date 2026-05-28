package com.dth.fashionshop.modules.catalog.controller;

import com.dth.fashionshop.modules.catalog.dto.request.ProductFilterRequest;
import com.dth.fashionshop.modules.catalog.dto.request.ProductRequest;
import com.dth.fashionshop.modules.catalog.dto.response.ProductDetailResponse;
import com.dth.fashionshop.modules.catalog.dto.response.ProductListAdminResponse;
import com.dth.fashionshop.modules.catalog.dto.response.ProductStatsResponse;
import com.dth.fashionshop.modules.catalog.dto.response.ProductSuggestionResponse;
import com.dth.fashionshop.modules.catalog.service.ProductService;
import com.dth.fashionshop.shared.utils.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin quản lý sản phẩm", description = "Các API dành cho Admin thực hiện các nghiệp vụ quản lý sản phẩm")
public class AdminProductController {

    private final ProductService productService;

    // Lấy danh sách cơ bản (Không dùng cho giao diện)
    @Operation(summary = "Lấy danh sách toàn bộ sản phẩm và phân trang")
    @GetMapping
    public ResponseEntity<Page<ProductDetailResponse>> getBasicProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        int pageNumber = PaginationUtils.correctPageNo(page);

        return ResponseEntity.ok(productService.getBasicProducts(pageNumber, size));
    }

    @Operation(summary = "Lấy thông tin chi tiết của sản phẩm")
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "Thêm mới sản phẩm")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDetailResponse> createProduct(
            @Parameter(description = "Dữ liệu thông tin sản phẩm (Payload JSON)")
            @RequestPart("product") @Valid ProductRequest request,

            @Parameter(description = "Ảnh đại diện của sản phẩm")
            @RequestPart("thumbnail") MultipartFile thumbnail,

            @Parameter(description = "Danh sách ảnh phụ của sản phẩm")
            @RequestPart(value = "images", required = false) List<MultipartFile> images)
    {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request, thumbnail, images));
    }

    @Operation(summary = "Cập nhật sản phẩm")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDetailResponse> updateProduct(
            @PathVariable Long id,

            @Parameter(description = "Dữ liệu thông tin sản phẩm (Payload JSON)")
            @RequestPart("product") @Valid ProductRequest request,

            @Parameter(description = "Ảnh đại diện mới của sản phẩm")
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,

            @Parameter(description = "Danh sách ảnh phụ mới của sản phẩm")
            @RequestPart(value = "images", required = false) List<MultipartFile> images)
    {
        return ResponseEntity.ok(productService.updateProduct(id, request, thumbnail, images));
    }

    @Operation(summary = "Xóa mềm sản phẩm và các phân loại của sản phẩm đó")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Xóa sản phẩm thành công!"));
    }

    @Operation(summary = "Khôi phục sản phẩm và các phân loại của sản phẩm đó")
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ProductDetailResponse> restoreProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.restoreProduct(id));
    }

    @Operation(summary = "Xóa riêng lẻ 1 ảnh phụ của sản phẩm")
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<?> deleteProductImage(@PathVariable Long imageId) {
        productService.deleteProductImage(imageId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa hình ảnh thành công!"));
    }

    @Operation(summary = "Thống kê nhanh sản phẩm",
            description = "Thống kê tổng số sản phẩm đang kinh doanh, đã hết hàng, sắp hết hàng")
    @GetMapping("/stats")
    public ResponseEntity<ProductStatsResponse> getProductStats() {
        return ResponseEntity.ok(productService.getProductStats());
    }

    @Operation(summary = "Lấy danh sách sản phẩm",
            description = "Hỗ trợ tìm kiếm theo keyword, lọc theo danh mục, trạng thái sản phẩm, tình trạng kho của sản phẩm và phân trang")
    @GetMapping("/search")
    public ResponseEntity<Page<ProductListAdminResponse>> searchAndFilterAdminProducts(
            @ModelAttribute ProductFilterRequest filter,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        int pageNumber = PaginationUtils.correctPageNo(page);

        return ResponseEntity.ok(productService.searchAndFilterAdminProducts(filter, pageNumber, size));
    }

    @Operation(summary = "Gợi ý tìm kiếm sản phẩm")
    @GetMapping("/suggestions")
    public ResponseEntity<List<ProductSuggestionResponse>> getProductSuggestions(
            @Parameter(description = "Gợi ý tìm kiếm theo tên sản phẩm và mã SKU của phân loại")
            @RequestParam String keyword)
    {
        return ResponseEntity.ok(productService.getAdminProductSuggestions(keyword));
    }
}