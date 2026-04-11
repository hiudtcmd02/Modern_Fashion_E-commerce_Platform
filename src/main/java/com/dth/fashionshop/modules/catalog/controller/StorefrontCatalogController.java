package com.dth.fashionshop.modules.catalog.controller;

import com.dth.fashionshop.modules.catalog.dto.response.ProductSuggestionResponse;
import com.dth.fashionshop.modules.catalog.dto.response.storefront.CategoryGuestResponse;
import com.dth.fashionshop.modules.catalog.dto.response.storefront.ProductDetailGuestResponse;
import com.dth.fashionshop.modules.catalog.dto.response.storefront.ProductGuestResponse;
import com.dth.fashionshop.modules.catalog.service.StorefrontService;
import com.dth.fashionshop.shared.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StorefrontCatalogController {

    private final StorefrontService storefrontService;

    // Lấy danh sách danh mục
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryGuestResponse>> getCategories() {
        return ResponseEntity.ok(storefrontService.getAllActiveCategories());
    }

    // Lấy top 8 sản phẩm mới nhất
    @GetMapping("/products/newest")
    public ResponseEntity<List<ProductGuestResponse>> getNewestProducts() {
        return ResponseEntity.ok(storefrontService.getNewestProducts());
    }

    // Lấy top 8 sản phẩm đánh giá cao nhất
    @GetMapping("/products/top-rated")
    public ResponseEntity<List<ProductGuestResponse>> getTopRatedProducts() {
        return ResponseEntity.ok(storefrontService.getTopRatedProducts());
    }

    // Gợi ý tìm kiếm cho Khách hàng
    @GetMapping("/products/suggestions")
    public ResponseEntity<List<ProductSuggestionResponse>> getSuggestions(
            @RequestParam String keyword) {
        return ResponseEntity.ok(storefrontService.getStorefrontSuggestions(keyword));
    }

    // Lấy danh sách sản phẩm, tìm kiếm, lọc theo danh mục và giá, sắp xếp, phân trang
    @GetMapping("/products")
    public ResponseEntity<Page<ProductGuestResponse>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        int pageNumber = PaginationUtils.correctPageNo(page);

        return ResponseEntity.ok(storefrontService.searchProducts(
                keyword, categoryId, minPrice, maxPrice, sort, pageNumber, size
        ));
    }

    // Lấy thông tin chi tiết sản phẩm cho khách hàng
    @GetMapping("/products/{slug}")
    public ResponseEntity<ProductDetailGuestResponse> getProductDetail(@PathVariable String slug) {
        return ResponseEntity.ok(storefrontService.getProductDetail(slug));
    }
}