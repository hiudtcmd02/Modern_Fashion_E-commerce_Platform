package com.dth.fashionshop.modules.order.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IpnResponse {
    @JsonProperty("RspCode")
    private String rspCode;

    @JsonProperty("Message")
    private String message;
}