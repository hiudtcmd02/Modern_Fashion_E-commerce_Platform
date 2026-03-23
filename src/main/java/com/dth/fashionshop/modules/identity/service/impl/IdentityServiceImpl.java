package com.dth.fashionshop.modules.identity.service.impl;

import com.dth.fashionshop.modules.identity.dto.request.RegisterRequest;
import com.dth.fashionshop.modules.identity.dto.request.VerifyOtpRequest;
import com.dth.fashionshop.modules.identity.entity.Role;
import com.dth.fashionshop.modules.identity.entity.User;
import com.dth.fashionshop.modules.identity.enums.UserStatus;
import com.dth.fashionshop.modules.identity.repository.RoleRepository;
import com.dth.fashionshop.modules.identity.repository.UserRepository;
import com.dth.fashionshop.modules.identity.service.IdentityService;
import com.dth.fashionshop.shared.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityServiceImpl implements IdentityService{

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional
    public void register(RegisterRequest request){
        log.info("Đang xử lý đăng ký cho email: {}", request.getEmail());

        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email này đã được sử dụng!");
        }
        if(userRepository.existsByPhoneNumber(request.getPhoneNumber())){
            throw new RuntimeException("Số điện thoại này đã được sử dụng!");
        }

        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Lỗi hệ thống: Không tìm thấy role ROLE_CUSTOMER trong Database"));

        String otpCode = generateOtpCode();

        User newUser = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .roles(java.util.Set.of(customerRole))
                .status(UserStatus.INACTIVE)
                .otpCode(otpCode)
                .otpExpiryTime(LocalDateTime.now().plusMinutes(5))
                .build();

        userRepository.save(newUser);

        emailService.sendOtpEmail(newUser.getEmail(), otpCode);

        log.info("Đã lưu User mới trạng thái INACTIVE và gửi kích hoạt tiến trình gửi OTP qua email");
    }

    @Override
    @Transactional
    public void verifyOtp(VerifyOtpRequest request){
        log.info("Đang xác thực OTP cho email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này"));

        if(user.getStatus() == UserStatus.ACTIVE){
            throw new RuntimeException("Tài khoản này đã được kích hoạt từ trước!");
        }
        if(!request.getOtpCode().equals(user.getOtpCode())){
            throw new RuntimeException("Mã OTP không chính xác!");
        }
        if(user.getOtpExpiryTime().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại mã mới!");
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setOtpCode(null);
        user.setOtpExpiryTime(null);

        userRepository.save(user);

        log.info("Tài khoản {} đã được kích hoạt thành công!", user.getEmail());
    }

    public String generateOtpCode(){
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}