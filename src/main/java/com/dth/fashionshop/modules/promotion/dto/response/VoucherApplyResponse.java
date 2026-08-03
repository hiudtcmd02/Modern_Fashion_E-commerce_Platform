package com.dth.fashionshop.modules.promotion.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class VoucherApplyResponse {

    @Schema(description = "Mã Voucher", example = "SUMMER26")
    private String code;

    @Schema(description = "Số tiền được giảm", example = "50000")
    private Long discountAmount;
}