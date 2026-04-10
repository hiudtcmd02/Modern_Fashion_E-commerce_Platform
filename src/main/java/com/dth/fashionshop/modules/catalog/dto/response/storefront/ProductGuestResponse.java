package com.dth.fashionshop.modules.catalog.dto.response.storefront;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductGuestResponse {
    private Long id;
    private String name;
    private String slug;
    private String thumbnailUrl;
    private Long minPrice;
    private Long maxPrice;
}