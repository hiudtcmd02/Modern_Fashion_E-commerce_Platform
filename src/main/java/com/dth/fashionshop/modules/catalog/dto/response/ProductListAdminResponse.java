package com.dth.fashionshop.modules.catalog.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductListAdminResponse {
    private Long id;
    private String name;
    private String thumbnailUrl;
    private String categoryName;

    private Long minPrice;
    private Long maxPrice;

    private Long totalStock;
    private Boolean hasOutOfStockSku;
    private Boolean hasLowStockSku;

    private Boolean isDeleted;
    private LocalDateTime createdAt;
}