package com.dth.fashionshop.modules.identity.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AddressResponse { // DTO này dùng cho cả module identity và module order
    private Long id;
    private String receiverName;
    private String receiverPhone;
    private String city;
    private String district;
    private String ward;
    private String street;
    private Boolean isDefault;
}