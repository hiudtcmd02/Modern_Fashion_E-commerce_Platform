package com.dth.fashionshop.modules.order.dto.request;

import com.dth.fashionshop.modules.order.enums.PaymentMethod;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateOrderRequest {
    @Schema(description = "ID của địa chỉ muốn chọn làm địa chỉ nhận hàng")
    @NotNull(message = "Vui lòng cung cấp địa chỉ nhận hàng")
    private Long addressId;

    @Schema(description = "Phương thức thanh toán (COD/VNPAY)", example = "COD")
    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private PaymentMethod paymentMethod;

    @Schema(description = "Danh sách các ID bên trong giỏ hàng của các sản phẩm muốn đặt hàng", example = "[1, 3]")
    @NotEmpty(message = "Danh sách sản phẩm không được để trống")
    private List<Long> cartItemIds;

    @Schema(description = "Tổng số tiền cần thanh toán được hiển thị trên giao diện checkout")
    @NotNull(message = "Hệ thống cần xác nhận tổng tiền cần thanh toán dự kiến từ giao diện")
    private Long expectedTotalAmount;

    private String customerNote;
}