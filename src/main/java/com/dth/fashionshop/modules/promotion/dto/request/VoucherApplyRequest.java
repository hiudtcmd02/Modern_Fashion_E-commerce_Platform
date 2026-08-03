package com.dth.fashionshop.modules.promotion.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class VoucherApplyRequest {

    @Schema(description = "Mã Voucher cần áp dụng", example = "SUMMER26")
    @NotBlank(message = "Vui lòng nhập mã khuyến mãi")
    private String code;

    @Schema(description = "Tổng tiền hàng các sản phẩm hiện tại của đơn hàng này", example = "500000")
    @NotNull(message = "Vui lòng cung cấp tổng tiền hàng để kiểm tra điều kiện")
    @Min(value = 0, message = "Tổng tiền hàng không được là số âm")
    private Long cartTotal;
}