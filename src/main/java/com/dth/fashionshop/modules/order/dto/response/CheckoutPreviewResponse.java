package com.dth.fashionshop.modules.order.dto.response;

import com.dth.fashionshop.modules.identity.dto.response.AddressResponse;
import com.dth.fashionshop.modules.cart.dto.response.CartItemCheckoutResponse;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CheckoutPreviewResponse {
    private AddressResponse shippingAddress;
    private List<CartItemCheckoutResponse> items;
    private Long totalAmount;
    private Long shippingFee;
    private Long finalAmount;
}