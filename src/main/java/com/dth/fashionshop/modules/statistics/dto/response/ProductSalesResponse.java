package com.dth.fashionshop.modules.statistics.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductSalesResponse {

    private Long productId;

    private String productName;

    private String thumbnailUrl;

    @Schema(description = "Tổng số lượng sản phẩm đã bán ra (chỉ tính đơn COMPLETED)", example = "150")
    private Long totalSold;

    @Schema(description = "Tổng doanh thu mang lại từ sản phẩm này", example = "45000000")
    private Long totalRevenue;

    @Schema(description = "Trạng thái xóa mềm của sản phẩm (true: Đã ngừng kinh doanh, false: Đang kinh doanh)", example = "false")
    private Boolean isDeleted;
}