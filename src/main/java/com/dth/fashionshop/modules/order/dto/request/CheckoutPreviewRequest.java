package com.dth.fashionshop.modules.order.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CheckoutPreviewRequest {
    @Schema(description = "ID của địa chỉ muốn chọn làm địa chỉ nhận hàng")
    private Long addressId;

    @Schema(description = "Danh sách các ID bên trong giỏ hàng của các sản phẩm muốn đặt hàng", example = "[1, 3]")
    @NotEmpty(message = "Vui lòng chọn sản phẩm để thanh toán")
    private List<Long> cartItemIds;
}