package com.dth.fashionshop.modules.catalog.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductDetailResponse {
    private Long id;
    private String name;
    private String slug;
    private Long categoryId;
    private String categoryName;
    private String thumbnailUrl;
    private String description;
    private Float averageRating;
    private Integer reviewCount;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<VariantResponse> variants;
    private List<ImageResponse> images;
}