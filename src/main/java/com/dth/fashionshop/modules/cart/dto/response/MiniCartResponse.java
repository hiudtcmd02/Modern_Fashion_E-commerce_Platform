package com.dth.fashionshop.modules.cart.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MiniCartResponse {
    private Integer totalCartItems;
    private Long totalTempPrice;
    private List<CartItemResponse> items;
}