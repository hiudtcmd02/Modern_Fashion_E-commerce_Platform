package com.dth.fashionshop.modules.catalog.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    private String name;

    @Schema(description = "Chuỗi định danh (slug)", example = "ao-nam")
    @NotBlank(message = "Chuỗi định danh (slug) không được để trống")
    @Size(max = 100, message = "Chuỗi định danh (slug) không được vượt quá 100 ký tự")
    private String slug;

    @Size(max = 255, message = "Mô tả ngắn gọn, không được vượt quá 255 ký tự")
    private String description;
}