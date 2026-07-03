package com.dth.fashionshop.modules.statistics.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RevenueChartResponse {

    @Schema(description = "Nhãn hiển thị trên trục X của biểu đồ (Ví dụ: '01/03/2026', '02/2026')", example = "01/03/2026")
    private String label;

    @Schema(description = "Tổng doanh thu tương ứng với nhãn thời gian đó", example = "15000000")
    private Long revenue;
}