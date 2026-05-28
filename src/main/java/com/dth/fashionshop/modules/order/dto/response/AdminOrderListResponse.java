package com.dth.fashionshop.modules.order.dto.response;

import com.dth.fashionshop.modules.order.enums.OrderStatus;
import com.dth.fashionshop.modules.order.enums.PaymentMethod;
import com.dth.fashionshop.modules.order.enums.PaymentStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AdminOrderListResponse {
    private Long id;
    private String orderCode;
    private String customerName;
    private String customerPhone;
    private String receiverName;
    private String receiverPhone;
    private Long finalAmount;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
}