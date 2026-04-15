package com.dth.fashionshop.modules.cart.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
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