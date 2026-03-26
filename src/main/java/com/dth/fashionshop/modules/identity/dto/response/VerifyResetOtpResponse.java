package com.dth.fashionshop.modules.identity.dto.response;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyResetOtpResponse {
    private String resetToken;
    private String message;
}