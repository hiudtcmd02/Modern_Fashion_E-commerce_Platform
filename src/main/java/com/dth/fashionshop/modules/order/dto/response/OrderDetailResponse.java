package com.dth.fashionshop.modules.order.dto.response;

import com.dth.fashionshop.modules.order.enums.OrderStatus;
import com.dth.fashionshop.modules.order.enums.PaymentMethod;
import com.dth.fashionshop.modules.order.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderDetailResponse {
    private Long id;
    private String orderCode;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;

    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private String customerNote;

    private Long totalAmount;
    private Long shippingFee;
    private Long finalAmount;

    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}