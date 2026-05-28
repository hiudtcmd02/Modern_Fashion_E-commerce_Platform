package com.dth.fashionshop.modules.catalog.controller;

import com.dth.fashionshop.modules.catalog.dto.request.CategoryRequest;
import com.dth.fashionshop.modules.catalog.dto.response.CategoryResponse;
import com.dth.fashionshop.modules.catalog.service.CategoryService;
import com.dth.fashionshop.shared.utils.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin quản lý danh mục sản phẩm", description = "Các API dành cho Admin thực hiện các nghiệp vụ quản lý danh mục sản phẩm")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Lấy danh sách danh mục sản phẩm", description = "Hỗ trợ tìm kiếm theo keyword, lọc theo trạng thái và phân trang")
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @Parameter(description = "Tìm kiếm theo tên danh mục")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Lọc theo trạng thái danh mục: Tất cả, Đang hoạt động (giá trị True), Đã ẩn (giá trị False)")
            @RequestParam(required = false) Boolean isDeleted,

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        int pageNumber = PaginationUtils.correctPageNo(page);
        return ResponseEntity.ok(categoryService.getAllCategories(keyword, isDeleted, pageNumber, size));
    }

    @Operation(summary = "Lấy thông tin chi tiết của danh mục")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(summary = "Thêm danh mục mới")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryResponse> createCategory(
            @Parameter(description = "Dữ liệu thông tin danh mục (Payload JSON)")
            @RequestPart("request") @Valid CategoryRequest request,

            @Parameter(description = "Ảnh đại diện danh mục")
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(categoryService.createCategory(request, file));
    }

    @Operation(summary = "Cập nhật thông tin danh mục")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,

            @Parameter(description = "Dữ liệu thông tin danh mục (Payload JSON)")
            @RequestPart("request") @Valid CategoryRequest request,

            @Parameter(description = "Ảnh đại diện mới của danh mục")
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request, file));
    }

    @Operation(summary = "Xóa mềm danh mục")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa danh mục thành công!"));
    }

    @Operation(summary = "Khôi phục danh mục bị xóa")
    @PatchMapping("/{id}/restore")
    public ResponseEntity<CategoryResponse> restoreCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.restoreCategory(id));
    }
}