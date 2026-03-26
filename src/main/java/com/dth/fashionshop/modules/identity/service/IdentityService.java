package com.dth.fashionshop.modules.identity.service;

import com.dth.fashionshop.modules.identity.dto.request.LoginRequest;
import com.dth.fashionshop.modules.identity.dto.request.RegisterRequest;
import com.dth.fashionshop.modules.identity.dto.request.ResendOtpRequest;
import com.dth.fashionshop.modules.identity.dto.request.VerifyOtpRequest;
import com.dth.fashionshop.modules.identity.dto.response.LoginResponse;

public interface IdentityService {
    void register(RegisterRequest request);

    void verifyOtp(VerifyOtpRequest request);

    void resendOtp(ResendOtpRequest request);

    LoginResponse login(LoginRequest request);

    void logout(String token);
}