package com.dth.fashionshop.modules.catalog.dto.response.storefront;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class VariantGuestResponse {
    private Long id;
    private String skuCode;
    private String variantName;
    private Long price;
    private Integer stockQuantity;
}