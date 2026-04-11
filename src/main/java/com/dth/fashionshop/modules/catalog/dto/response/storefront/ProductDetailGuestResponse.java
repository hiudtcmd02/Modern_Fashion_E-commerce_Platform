package com.dth.fashionshop.modules.catalog.dto.response.storefront;

import com.dth.fashionshop.modules.catalog.dto.response.ImageResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductDetailGuestResponse {
    private Long id;
    private String name;
    private String slug;
    private String thumbnailUrl;
    private String description;
    private Long minPrice;
    private Long maxPrice;
    private Float averageRating;
    private Integer reviewCount;

    private List<ImageResponse> images;
    private List<VariantGuestResponse> variants;
}