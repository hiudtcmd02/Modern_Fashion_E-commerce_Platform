package com.dth.fashionshop.modules.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Chuỗi định danh (slug) không được để trống")
    @Size(max = 100, message = "Chuỗi định danh (slug) không được vượt quá 100 ký tự")
    private String slug;

    @Size(max = 255, message = "Mô tả ngắn gọn, không được vượt quá 255 ký tự")
    private String description;
}