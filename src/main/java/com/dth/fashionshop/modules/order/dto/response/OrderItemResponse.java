package com.dth.fashionshop.modules.order.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponse {
    private Long variantId;
    private String skuCode;
    private String productName;
    private String variantName;
    private String thumbnailUrl;
    private Long unitPrice;
    private Integer quantity;
    private Long subtotal;
}