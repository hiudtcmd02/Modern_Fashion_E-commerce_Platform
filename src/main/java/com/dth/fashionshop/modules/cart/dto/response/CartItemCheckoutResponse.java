package com.dth.fashionshop.modules.cart.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
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