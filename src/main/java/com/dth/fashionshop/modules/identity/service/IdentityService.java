package com.dth.fashionshop.modules.identity.service;

import com.dth.fashionshop.modules.identity.dto.request.RegisterRequest;
import com.dth.fashionshop.modules.identity.dto.request.VerifyOtpRequest;

public interface IdentityService {
    void register(RegisterRequest request);
    void verifyOtp(VerifyOtpRequest request);
}