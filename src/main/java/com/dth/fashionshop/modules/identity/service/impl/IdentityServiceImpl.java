package com.dth.fashionshop.modules.identity.service.impl;

import com.dth.fashionshop.modules.identity.dto.request.LoginRequest;
import com.dth.fashionshop.modules.identity.dto.request.RegisterRequest;
import com.dth.fashionshop.modules.identity.dto.request.ResendOtpRequest;
import com.dth.fashionshop.modules.identity.dto.request.VerifyOtpRequest;
import com.dth.fashionshop.modules.identity.dto.response.LoginResponse;
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
                .roles(java.util.Set.of(customerRole))
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

    @Override
    @Transactional
    public void resendOtp(ResendOtpRequest request) {
        log.info("Yêu cầu gửi lại OTP cho email: {}", request.getEmail());

        // 1. Tìm User trong Database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này!"));

        // 2. Chặn nếu tài khoản đã kích hoạt
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản này đã được kích hoạt! Vui lòng đăng nhập.");
        }

        // 3. THUẬT TOÁN CHỐNG SPAM (Cooldown 60 giây)
        if (user.getLastOtpSentAt() != null) {
            // Tính số giây trôi qua kể từ lần gửi cuối cùng
            long secondsSinceLastSent = ChronoUnit.SECONDS.between(user.getLastOtpSentAt(), LocalDateTime.now());

            if (secondsSinceLastSent < 60) {
                long waitTime = 60 - secondsSinceLastSent;
                throw new RuntimeException("Vui lòng đợi " + waitTime + " giây trước khi yêu cầu gửi lại mã mới.");
            }
        }

        // 4. Sinh mã OTP mới, gia hạn thêm 5 phút và cập nhật lại thời điểm gửi
        String newOtpCode = generateOtpCode();
        user.setOtpCode(newOtpCode);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        user.setLastOtpSentAt(LocalDateTime.now()); // <--- Cập nhật lại cột này bằng giờ hiện tại

        // 5. Lưu DB và Bắn luồng gửi Email
        userRepository.save(user);
        emailService.sendOtpEmail(user.getEmail(), newOtpCode);

        log.info("Đã cập nhật mã OTP mới và gửi email thành công cho: {}", user.getEmail());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Tiến hành xác thực đăng nhập cho email: {}", request.getEmail());

        try { // 1. Giao toàn quyền cho Giám đốc An ninh (Kiểm tra cả Mật khẩu lẫn trạng thái ACTIVE)
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

        // 2. Nếu code chạy được xuống đây, chắc chắn 100% tài khoản hợp lệ, mật khẩu ĐÚNG và đã ACTIVE.
        // Chỉ cần lấy User lên để đúc thẻ JWT (Không cần if-else kiểm tra status thủ công nữa!!!)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));

        // 3. Mọi thứ hoàn hảo -> Nhờ Máy đúc thẻ tạo JWT
        String jwtToken = jwtService.generateToken(user);
        log.info("Đăng nhập thành công, đã cấp Token cho: {}", user.getEmail());

        // 4. Đóng gói trả về cho Frontend
        return LoginResponse.builder()
                .accessToken(jwtToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }

    @Override
    public void logout(String token) {
        // 1. Lấy ngày hết hạn của token hiện tại
        Date expirationDate = jwtService.extractExpiration(token);

        // 2. Nhét vào danh sách đen
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(token)
                .expiryTime(expirationDate)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);
        log.info("Token đã được đưa vào danh sách đen! Người dùng đăng xuất thành công");
    }

    public String generateOtpCode(){
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}