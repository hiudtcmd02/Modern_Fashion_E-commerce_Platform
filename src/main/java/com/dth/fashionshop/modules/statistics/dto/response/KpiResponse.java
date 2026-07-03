package com.dth.fashionshop.modules.statistics.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KpiResponse {

    @Schema(description = "Tổng doanh thu trong tháng này (chỉ tính đơn COMPLETED)")
    private Long totalRevenue;

    @Schema(description = "Tỷ lệ tăng/giảm doanh thu so với tháng trước (%). Số dương là tăng, âm là giảm.", example = "15.5")
    private Double revenueGrowthPercentage;

    @Schema(description = "Tổng số đơn hàng đã hoàn thành trong tháng này")
    private Long totalCompletedOrders;

    @Schema(description = "Số lượng khách hàng mới đăng ký trong tháng này")
    private Long newCustomers;

    @Schema(description = "Số lượng phân loại sản phẩm (SKU) sắp hoặc đã hết hàng")
    private Long lowStockSkuCount;
}