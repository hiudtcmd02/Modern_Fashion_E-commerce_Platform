package com.dth.fashionshop.modules.catalog.dto.response.storefront;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VariantGuestResponse {
    private Long id;
    private String skuCode;
    private String variantName;
    private Long price;
    private Integer stockQuantity;
}