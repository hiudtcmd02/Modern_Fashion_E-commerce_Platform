package com.dth.fashionshop.modules.cart.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AddToCartRequest {

    @Schema(description = "Mã SKU của phân loại sản phẩm muốn thêm vào giỏ hàng", example = "POLO-DO-M")
    @NotBlank(message = "Mã SKU không được để trống")
    private String skuCode;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
    private Integer quantity;
}