package com.dth.fashionshop.modules.catalog.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String thumbnailUrl;
    private String description;
    private Boolean isDeleted;
}