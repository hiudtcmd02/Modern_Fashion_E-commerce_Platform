package com.dth.fashionshop.modules.catalog.dto.request;

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
public class ProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @NotBlank(message = "Chuỗi định danh (slug) không được để trống")
    private String slug;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    private String description;

    @NotEmpty(message = "Sản phẩm phải có ít nhất 1 phân loại")
    @Valid
    private List<VariantRequest> variants;
}