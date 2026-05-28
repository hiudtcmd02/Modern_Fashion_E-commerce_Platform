package com.dth.fashionshop.modules.catalog.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class VariantRequest {

    @Schema(description = "Mã SKU", example = "POLO-DO-M")
    @NotBlank(message = "Mã SKU không được để trống")
    private String skuCode;

    @Schema(description = "Tên phân loại", example = "Đỏ - M")
    @NotBlank(message = "Tên phân loại không được để trống (VD: Đỏ - M)")
    private String variantName;

    @NotNull(message = "Giá bán không được để trống")
    @Min(value = 0, message = "Giá bán phải lớn hơn hoặc bằng 0")
    private Long price;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho phải lớn hơn hoặc bằng 0")
    private Integer stockQuantity;

    @Schema(description = "Trạng thái hoạt động (TRUE - Đang hoạt động, FALSE - Đã ẩn)")
    @NotNull(message = "Trạng thái hoạt động không được để trống")
    private Boolean isActive;
}