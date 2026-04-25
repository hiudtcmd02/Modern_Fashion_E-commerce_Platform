package com.dth.fashionshop.modules.order.dto.response;

import com.dth.fashionshop.modules.order.enums.OrderStatus;
import com.dth.fashionshop.modules.order.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderListResponse {
    private Long id;
    private String orderCode;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private Long finalAmount;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
    private Integer totalItems;
}