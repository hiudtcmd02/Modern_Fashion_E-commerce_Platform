package com.dth.fashionshop.modules.catalog.dto.response.storefront;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductGuestResponse {
    private Long id;
    private String name;
    private String slug;
    private String thumbnailUrl;
    private Long minPrice;
    private Long maxPrice;
}