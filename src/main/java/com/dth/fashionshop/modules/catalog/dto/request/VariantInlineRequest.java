package com.dth.fashionshop.modules.catalog.dto.request;

import lombok.Data;

@Data
public class VariantInlineRequest {
    private Integer stockQuantity;
    private Boolean isActive;
}