package com.dth.fashionshop.modules.order.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaymentResultResponse {
    private Boolean isSuccess;
    private String message;
    private String orderCode;
    private Long amount;
}