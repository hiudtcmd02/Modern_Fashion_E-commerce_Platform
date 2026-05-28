package com.dth.fashionshop.modules.catalog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProductFilterRequest {

    @Schema(description = "Tìm kiếm theo tên sản phẩm và mã SKU")
    private String keyword;

    @Schema(description = "ID của danh mục cần lọc", example = "1")
    private Long categoryId;

    @Schema(description = "Lọc theo trạng thái của sản phẩm " +
            "(Đang hoạt động - ACTIVE, " +
            "Đã ẩn - HIDDEN, " +
            "Tất cả - không gửi lên tham số này.)",
            example = "ACTIVE")
    private String status;

    @Schema(description = "Lọc theo tình trạng kho " +
            "(Hết hàng toàn bộ (sản phẩm không còn hàng, tổng tồn kho là 0) - OUT_OF_STOCK, " +
            "Có SKU hết hàng - HAS_OUT_OF_STOCK_SKU, " +
            "Có SKU sắp hết hàng - HAS_LOW_STOCK_SKU, " +
            "Tất cả - không gửi lên tham số này.)",
            example = "OUT_OF_STOCK")
    private String inventoryStatus;
}