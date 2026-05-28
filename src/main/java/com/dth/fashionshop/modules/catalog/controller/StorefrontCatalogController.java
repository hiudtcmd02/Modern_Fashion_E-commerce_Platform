package com.dth.fashionshop.modules.catalog.controller;

import com.dth.fashionshop.modules.catalog.dto.response.ProductSuggestionResponse;
import com.dth.fashionshop.modules.catalog.dto.response.storefront.CategoryGuestResponse;
import com.dth.fashionshop.modules.catalog.dto.response.storefront.ProductDetailGuestResponse;
import com.dth.fashionshop.modules.catalog.dto.response.storefront.ProductGuestResponse;
import com.dth.fashionshop.modules.catalog.service.StorefrontService;
import com.dth.fashionshop.shared.utils.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Khách hàng khám phá, tìm kiếm và xem chi tiết sản phẩm",
        description = "Các API giúp cho khách hàng không cần đăng nhập vẫn có thể khám phá các sản phẩm " +
                "mới nhất, được đánh giá cao nhất, tìm kiếm sản phẩm, xem chi tiết sản phẩm.")
public class StorefrontCatalogController {

    private final StorefrontService storefrontService;

    @Operation(summary = "Lấy danh sách danh mục đang hoạt động")
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryGuestResponse>> getCategories() {
        return ResponseEntity.ok(storefrontService.getAllActiveCategories());
    }

    @Operation(summary = "Lấy top 8 sản phẩm mới nhất")
    @GetMapping("/products/newest")
    public ResponseEntity<List<ProductGuestResponse>> getNewestProducts() {
        return ResponseEntity.ok(storefrontService.getNewestProducts());
    }

    @Operation(summary = "Lấy top 8 sản phẩm được đánh giá cao nhất")
    @GetMapping("/products/top-rated")
    public ResponseEntity<List<ProductGuestResponse>> getTopRatedProducts() {
        return ResponseEntity.ok(storefrontService.getTopRatedProducts());
    }

    @Operation(summary = "Gợi ý tìm kiếm sản phẩm")
    @GetMapping("/products/suggestions")
    public ResponseEntity<List<ProductSuggestionResponse>> getSuggestions(
            @Parameter(description = "Gợi ý tìm kiếm theo tên của sản phẩm")
            @RequestParam String keyword)
    {
        return ResponseEntity.ok(storefrontService.getStorefrontSuggestions(keyword));
    }

    @Operation(summary = "Lấy danh sách sản phẩm",
            description = "Hỗ trợ tìm kiếm theo keyword, lọc theo danh mục và giá, sắp xếp và phân trang")
    @GetMapping("/products")
    public ResponseEntity<Page<ProductGuestResponse>> searchProducts(
            @Parameter(description = "Tìm kiếm theo tên sản phẩm")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "ID của danh mục cần lọc", example = "1")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "Mức giá thấp nhất")
            @RequestParam(required = false) Long minPrice,

            @Parameter(description = "Mức giá cao nhất")
            @RequestParam(required = false) Long maxPrice,

            @Parameter(description = "Kiểu sắp xếp: " +
                    "Mới nhất (Mặc định) [newest], " +
                    "Cũ nhất [oldest], " +
                    "Giá: Từ thấp đến cao [price_asc], " +
                    "Giá: Từ cao đến thấp [price_desc]")
            @RequestParam(required = false, defaultValue = "newest") String sort,

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        int pageNumber = PaginationUtils.correctPageNo(page);

        return ResponseEntity.ok(storefrontService.searchProducts(
                keyword, categoryId, minPrice, maxPrice, sort, pageNumber, size
        ));
    }

    @Operation(summary = "Lấy thông tin chi tiết sản phẩm")
    @GetMapping("/products/{slug}")
    public ResponseEntity<ProductDetailGuestResponse> getProductDetail(
            @Parameter(description = "Chuỗi định danh (slug)", example = "ao-polo-co-tron")
            @PathVariable String slug)
    {
        return ResponseEntity.ok(storefrontService.getProductDetail(slug));
    }
}