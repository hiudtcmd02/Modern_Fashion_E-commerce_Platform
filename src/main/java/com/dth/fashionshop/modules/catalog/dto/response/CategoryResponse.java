package com.dth.fashionshop.modules.catalog.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String thumbnailUrl;
    private String description;
    private Boolean isDeleted;
}