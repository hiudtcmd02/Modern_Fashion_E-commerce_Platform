package com.dth.fashionshop.modules.catalog.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Dữ liệu thông tin cập nhật của phân loại (biến thể) (Payload JSON)")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class VariantInlineRequest {

    @Schema(description = "Giá trị tồn kho mới. Dùng cho API cập nhật nhanh tồn kho trên giao diện danh sách sản phẩm của Admin")
    private Integer stockQuantity;

    @Schema(description = "Trạng thái hoạt động mới. Dùng cho API thay đổi nhanh trạng thái cho phân loại trên giao diện danh sách sản phẩm của Admin")
    private Boolean isActive;
}