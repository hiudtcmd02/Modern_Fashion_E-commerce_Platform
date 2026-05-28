package com.dth.fashionshop.modules.cart.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MiniCartResponse {
    private Integer totalCartItems;
    private Long totalTempPrice;
    private List<CartItemResponse> items;
}