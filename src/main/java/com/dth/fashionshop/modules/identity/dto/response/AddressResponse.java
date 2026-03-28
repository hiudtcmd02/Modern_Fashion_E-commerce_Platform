package com.dth.fashionshop.modules.identity.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponse {
    private Long id;
    private String receiverName;
    private String receiverPhone;
    private String city;
    private String district;
    private String ward;
    private String street;
    private Boolean isDefault;
}