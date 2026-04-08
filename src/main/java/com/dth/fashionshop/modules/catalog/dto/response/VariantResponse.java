package com.dth.fashionshop.modules.catalog.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantResponse {
    private Long id;
    private String skuCode;
    private String variantName;
    private Long price;
    private Integer stockQuantity;
    private Boolean isActive;
}