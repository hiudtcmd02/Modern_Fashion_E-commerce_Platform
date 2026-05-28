package com.dth.fashionshop.modules.order.dto.request;

import com.dth.fashionshop.modules.order.enums.OrderStatus;
import com.dth.fashionshop.modules.order.enums.PaymentStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateOrderStatusRequest {

    @Schema(description = "Trạng thái đơn hàng: PENDING, PROCESSING, SHIPPING, COMPLETED, CANCELLED, RETURNED")
    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    private OrderStatus orderStatus;

    @Schema(description = "Trạng thái thanh toán: UNPAID, PAID, REFUNDED")
    @NotNull(message = "Trạng thái thanh toán không được để trống")
    private PaymentStatus paymentStatus;

    private String internalNote;
}