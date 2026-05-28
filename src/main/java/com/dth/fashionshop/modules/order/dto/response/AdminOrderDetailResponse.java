package com.dth.fashionshop.modules.order.dto.response;

import com.dth.fashionshop.modules.order.enums.OrderStatus;
import com.dth.fashionshop.modules.order.enums.PaymentMethod;
import com.dth.fashionshop.modules.order.enums.PaymentStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AdminOrderDetailResponse {
    private Long id;
    private String orderCode;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;

    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;

    private String customerNote;
    private String internalNote;

    private Long totalAmount;
    private Long shippingFee;
    private Long finalAmount;

    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}