package com.dth.fashionshop.modules.cart.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CartItemCheckoutResponse {
    private Long cartItemId;
    private Long variantId;
    private String skuCode;
    private String productName;
    private String variantName;
    private String thumbnailUrl;
    private Integer quantity;
    private Long unitPrice;
}