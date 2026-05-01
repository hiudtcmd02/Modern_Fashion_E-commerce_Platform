package com.dth.fashionshop.modules.order.dto.request;

import com.dth.fashionshop.modules.order.enums.OrderStatus;
import com.dth.fashionshop.modules.order.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    private OrderStatus orderStatus;

    @NotNull(message = "Trạng thái thanh toán không được để trống")
    private PaymentStatus paymentStatus;

    private String internalNote;
}