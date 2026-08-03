package com.dth.fashionshop.modules.promotion.dto.response;

import com.dth.fashionshop.modules.promotion.enums.DiscountType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class VoucherGuestResponse {
    private String code;
    private String name;
    private String description;
    private DiscountType discountType;
    private Long discountValue;
    private Long maxDiscountAmount;
    private Long minOrderValue;
    private Integer usageLimit;

    @Schema(description = "Tổng số lượt đã dùng của Voucher")
    private Integer usedCount;

    @Schema(description = "Thời gian bắt đầu", example = "2026-03-10 00:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    @Schema(description = "Thời gian kết thúc", example = "2026-03-20 23:59:59")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    @Schema(description = "Trạng thái hiển thị (Sắp diễn ra / Đang diễn ra)")
    public String getDisplayStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(this.startDate)) {
            return "Sắp diễn ra";
        }
        return "Đang diễn ra";
    }
}