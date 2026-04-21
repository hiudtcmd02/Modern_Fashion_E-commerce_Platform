package com.dth.fashionshop.modules.order.dto.request;

import com.dth.fashionshop.modules.order.enums.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    @NotNull(message = "Vui lòng cung cấp địa chỉ nhận hàng")
    private Long addressId;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private PaymentMethod paymentMethod;

    @NotEmpty(message = "Danh sách sản phẩm không được để trống")
    private List<Long> cartItemIds;

    @NotNull(message = "Hệ thống cần xác nhận tổng tiền cần thanh toán dự kiến từ giao diện")
    private Long expectedTotalAmount;

    private String customerNote;
}