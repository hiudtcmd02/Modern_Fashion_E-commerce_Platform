package com.dth.fashionshop.modules.catalog.controller;

import com.dth.fashionshop.modules.catalog.dto.request.CategoryRequest;
import com.dth.fashionshop.modules.catalog.dto.response.CategoryResponse;
import com.dth.fashionshop.modules.catalog.service.CategoryService;
import com.dth.fashionshop.shared.utils.PaginationUtils;
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
public class AdminCategoryController {

    private final CategoryService categoryService;

    // Lấy danh sách danh mục (tìm kiếm, phân trang, lọc trạng thái)
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        int pageNumber = PaginationUtils.correctPageNo(page);
        return ResponseEntity.ok(categoryService.getAllCategories(keyword, isDeleted, pageNumber, size));
    }

    // Lấy thông tin chi tiết một danh mục
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // Thêm mới danh mục
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestPart("request") @Valid CategoryRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(categoryService.createCategory(request, file));
    }

    // Cập nhật danh mục
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestPart("request") @Valid CategoryRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request, file));
    }

    // Xóa mềm danh mục
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa danh mục thành công!"));
    }

    // Khôi phục danh mục
    @PatchMapping("/{id}/restore")
    public ResponseEntity<CategoryResponse> restoreCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.restoreCategory(id));
    }
}