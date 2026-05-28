package com.dth.fashionshop.modules.cart.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CartItemResponse {
    private Long id;
    private String skuCode;
    private String productName;
    private String variantName;
    private String thumbnailUrl;
    private Long unitPrice;
    private Integer quantity;
    private Integer currentStock;

    private Boolean isAvailable;
    private Boolean hasError;
    private String errorMessage;
}