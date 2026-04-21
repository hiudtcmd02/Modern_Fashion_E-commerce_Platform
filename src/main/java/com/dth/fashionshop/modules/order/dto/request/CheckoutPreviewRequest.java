package com.dth.fashionshop.modules.order.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class CheckoutPreviewRequest {
    private Long addressId;

    @NotEmpty(message = "Vui lòng chọn sản phẩm để thanh toán")
    private List<Long> cartItemIds;
}