package com.dth.fashionshop.modules.identity.service.impl;

import com.dth.fashionshop.modules.identity.dto.request.*;
import com.dth.fashionshop.modules.identity.dto.response.LoginResponse;
import com.dth.fashionshop.modules.identity.dto.response.VerifyResetOtpResponse;
import com.dth.fashionshop.modules.identity.entity.InvalidatedToken;
import com.dth.fashionshop.modules.identity.entity.Role;
import com.dth.fashionshop.modules.identity.entity.User;
import com.dth.fashionshop.modules.identity.enums.UserStatus;
import com.dth.fashionshop.modules.identity.repository.InvalidatedTokenRepository;
import com.dth.fashionshop.modules.identity.repository.RoleRepository;
import com.dth.fashionshop.modules.identity.repository.UserRepository;
import com.dth.fashionshop.modules.identity.service.IdentityService;
import com.dth.fashionshop.modules.identity.service.JwtService;
import com.dth.fashionshop.shared.email.EmailService;
import com.dth.fashionshop.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Random;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityServiceImpl implements IdentityService{

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

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
                .roles(Set.of(customerRole))
                .status(UserStatus.INACTIVE)
                .otpCode(otpCode)
                .otpExpiryTime(LocalDateTime.now().plusMinutes(5))
                .lastOtpSentAt(LocalDateTime.now())
                .build();

        userRepository.save(newUser);

        emailService.sendOtpEmail(newUser.getEmail(), otpCode);

        log.info("Đã lưu User mới với trạng thái INACTIVE và kích hoạt tiến trình gửi OTP qua email");
    }

    @Override
    @Transactional
    public void verifyOtp(VerifyOtpRequest request){
        log.info("Đang xác thực OTP cho email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email này"));

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

    @Override
    @Transactional
    public void resendOtp(ResendOtpRequest request) {
        log.info("Yêu cầu gửi lại OTP cho email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email này!"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản này đã được kích hoạt! Vui lòng đăng nhập.");
        }

        if (user.getLastOtpSentAt() != null) {
            long secondsSinceLastSent = ChronoUnit.SECONDS.between(user.getLastOtpSentAt(), LocalDateTime.now());

            if (secondsSinceLastSent < 60) {
                long waitTime = 60 - secondsSinceLastSent;
                throw new RuntimeException("Vui lòng đợi " + waitTime + " giây trước khi yêu cầu gửi lại mã mới.");
            }
        }

        String newOtpCode = generateOtpCode();
        user.setOtpCode(newOtpCode);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        user.setLastOtpSentAt(LocalDateTime.now());

        userRepository.save(user);
        emailService.sendOtpEmail(user.getEmail(), newOtpCode);

        log.info("Đã cập nhật mã OTP mới và gửi email thành công cho: {}", user.getEmail());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Tiến hành xác thực đăng nhập cho email: {}", request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (DisabledException e) {
            throw new RuntimeException("Tài khoản chưa được xác thực OTP. Vui lòng kiểm tra email!");
        } catch (LockedException e) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin!");
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Email hoặc mật khẩu không chính xác!");
        } catch (AuthenticationException e) {
            throw new RuntimeException("Đăng nhập thất bại, vui lòng thử lại!");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản!"));

        String jwtToken = jwtService.generateToken(user);
        log.info("Đăng nhập thành công, đã cấp Token cho: {}", user.getEmail());

        return LoginResponse.builder()
                .accessToken(jwtToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }

    @Override
    public void logout(String token) {

        Date expirationDate = jwtService.extractExpiration(token);

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(token)
                .expiryTime(expirationDate)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);
        log.info("Token đã được đưa vào danh sách đen! Người dùng đăng xuất thành công");
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Xử lý yêu cầu quên mật khẩu cho email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy email trong hệ thống"));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new RuntimeException("Tài khoản của bạn đang bị khóa, không thể cấp lại mật khẩu. Vui lòng liên hệ Admin!");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new RuntimeException("Tài khoản của bạn chưa được xác thực email! Vui lòng xác thực email!");
        }

        if (user.getLastOtpSentAt() != null) {
            long secondsSinceLastSent = ChronoUnit.SECONDS.between(user.getLastOtpSentAt(), LocalDateTime.now());

            if (secondsSinceLastSent < 60) {
                long waitTime = 60 - secondsSinceLastSent;
                throw new RuntimeException("Vui lòng đợi " + waitTime + " giây trước khi yêu cầu gửi lại mã mới.");
            }
        }

        String otpCode = generateOtpCode();

        user.setOtpCode(otpCode);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        user.setLastOtpSentAt(LocalDateTime.now());
        userRepository.save(user);

        emailService.sendForgotPasswordEmail(user.getEmail(), otpCode);

        log.info("Đã gửi mã OTP khôi phục mật khẩu tới email: {}", request.getEmail());
    }

    @Override
    public VerifyResetOtpResponse verifyResetOtp(VerifyResetOtpRequest request) {
        log.info("Xác thực OTP khôi phục mật khẩu cho email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản!"));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ với Admin!");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new RuntimeException("Tài khoản của bạn chưa được xác thực!");
        }

        if (user.getOtpExpiryTime() == null || user.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới!");
        }

        if (!user.getOtpCode().equals(request.getOtp())) {
            int failedAttempts = (user.getOtpFailedAttempts() == null ? 0 : user.getOtpFailedAttempts()) + 1;
            user.setOtpFailedAttempts(failedAttempts);

            if (failedAttempts >= 5) {
                user.setStatus(UserStatus.LOCKED);
                user.setOtpCode(null);
                userRepository.save(user);
                throw new RuntimeException("Bạn đã nhập sai OTP 5 lần. Tài khoản đã bị khóa để bảo mật. Vui lòng liên hệ Admin để mở khóa!");
            }

            userRepository.save(user);
            throw new RuntimeException("Mã OTP không chính xác! Bạn còn " + (5 - failedAttempts) + " lần thử.");
        }

        user.setOtpFailedAttempts(0);
        user.setOtpCode(null);
        user.setOtpExpiryTime(null);
        userRepository.save(user);

        String resetToken = jwtService.generateResetToken(user);

        return VerifyResetOtpResponse.builder()
                .resetToken(resetToken)
                .message("Xác thực thành công. Vui lòng tiến hành đặt lại mật khẩu!")
                .build();
    }

    @Override
    public void resetPassword(String token, ResetPasswordRequest request) {

        if (isTokenInvalidated(token)) {
            throw new RuntimeException("Token khôi phục này đã được sử dụng hoặc không còn hợp lệ!");
        }

        String purpose = jwtService.extractPurpose(token);
        if (!"RESET_PASSWORD".equals(purpose)) {
            throw new RuntimeException("Token không hợp lệ! Vui lòng thực hiện lại luồng Quên mật khẩu.");
        }

        String email = jwtService.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản!"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(token)
                .expiryTime(jwtService.extractExpiration(token))
                .build();
        invalidatedTokenRepository.save(invalidatedToken);

        log.info("Đã đổi mật khẩu thành công và thu hồi Reset Token cho email: {}", email);
    }

    // Hàm kiểm tra xem token có nằm trong blacklist không
    @Override
    public boolean isTokenInvalidated(String token) {
        return invalidatedTokenRepository.existsById(token);
    }

    public String generateOtpCode(){
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}