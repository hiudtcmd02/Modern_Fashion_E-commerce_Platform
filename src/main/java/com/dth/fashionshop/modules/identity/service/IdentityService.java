package com.dth.fashionshop.modules.identity.service;

import com.dth.fashionshop.modules.identity.dto.request.*;
import com.dth.fashionshop.modules.identity.dto.response.LoginResponse;
import com.dth.fashionshop.modules.identity.dto.response.VerifyResetOtpResponse;

public interface IdentityService {
    void register(RegisterRequest request);

    void verifyOtp(VerifyOtpRequest request);

    void resendOtp(ResendOtpRequest request);

    LoginResponse login(LoginRequest request);

    void logout(String token);

    void forgotPassword(ForgotPasswordRequest request);

    VerifyResetOtpResponse verifyResetOtp(VerifyResetOtpRequest request);

    void resetPassword(String token, ResetPasswordRequest request);

    boolean isTokenInvalidated(String token);
}