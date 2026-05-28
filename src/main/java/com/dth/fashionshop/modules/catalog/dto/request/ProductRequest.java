package com.dth.fashionshop.modules.catalog.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @Schema(description = "Chuỗi định danh (slug)", example = "ao-polo-co-tron")
    @NotBlank(message = "Chuỗi định danh (slug) không được để trống")
    private String slug;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    private String description;

    @Schema(description = "Danh sách phân loại của sản phẩm")
    @NotEmpty(message = "Sản phẩm phải có ít nhất 1 phân loại")
    @Valid
    private List<VariantRequest> variants;
}