package com.dth.fashionshop.modules.order.dto.response;

import com.dth.fashionshop.modules.order.enums.OrderStatus;
import com.dth.fashionshop.modules.order.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String orderCode;
    private Long finalAmount;
    private OrderStatus orderStatus;
    private PaymentMethod paymentMethod;
    private String message;
}