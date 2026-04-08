package com.dth.fashionshop.modules.catalog.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductSuggestionResponse {
    private Long id;
    private String name;
    private String thumbnailUrl;
}