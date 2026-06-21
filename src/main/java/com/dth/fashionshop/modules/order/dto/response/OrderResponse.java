package com.dth.fashionshop.modules.order.dto.response;

import com.dth.fashionshop.modules.order.enums.OrderStatus;
import com.dth.fashionshop.modules.order.enums.PaymentMethod;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderResponse {
    private Long id;
    private String orderCode;
    private Long finalAmount;
    private OrderStatus orderStatus;
    private PaymentMethod paymentMethod;
    private String message;

    private String paymentUrl;
}