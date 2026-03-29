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

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Xử lý yêu cầu quên mật khẩu cho email: {}", request.getEmail());

        // 1. Tìm user trong Database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy email trong hệ thống"));

        // 2. Chặn những tài khoản đang bị khóa (LOCKED) và chưa xác thực (INACTIVE)
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new RuntimeException("Tài khoản của bạn đang bị khóa, không thể cấp lại mật khẩu!");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new RuntimeException("Tài khoản của bạn chưa được xác thực email! Vui lòng xác thực email!");
        }

        // CHỐT CHẶN CHỐNG SPAM (1 PHÚT)
        if (user.getLastOtpSentAt() != null) {
            // Tính số giây trôi qua kể từ lần gửi cuối cùng
            long secondsSinceLastSent = ChronoUnit.SECONDS.between(user.getLastOtpSentAt(), LocalDateTime.now());

            if (secondsSinceLastSent < 60) {
                long waitTime = 60 - secondsSinceLastSent;
                throw new RuntimeException("Vui lòng đợi " + waitTime + " giây trước khi yêu cầu gửi lại mã mới.");
            }
        }

        // 3. Sinh mã OTP mới
        String otpCode = generateOtpCode();

        // 4. Lưu OTP, Cập nhật thời gian hết hạn và thời gian lần cuối gửi OTP
        user.setOtpCode(otpCode);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        user.setLastOtpSentAt(LocalDateTime.now());
        userRepository.save(user);

        // 5. Gọi cỗ máy gửi Email
        emailService.sendForgotPasswordEmail(user.getEmail(), otpCode);

        log.info("Đã gửi mã OTP khôi phục mật khẩu tới email: {}", request.getEmail());
    }

    @Override
    public VerifyResetOtpResponse verifyResetOtp(VerifyResetOtpRequest request) {
        log.info("Xác thực OTP khôi phục mật khẩu cho email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa!");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new RuntimeException("Tài khoản của bạn chưa được xác thực!");
        }

        // 1. Kiểm tra xem OTP có bị quá hạn không (Quá 5 phút)
        if (user.getOtpExpiryTime() == null || user.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới!");
        }

        // 2. Kiểm tra xem OTP nhập vào có ĐÚNG không
        if (!user.getOtpCode().equals(request.getOtp())) {
            // NẾU SAI: Tăng biến đếm lên 1
            int failedAttempts = (user.getOtpFailedAttempts() == null ? 0 : user.getOtpFailedAttempts()) + 1;
            user.setOtpFailedAttempts(failedAttempts);

            // Nếu sai đủ 5 lần -> Rút thẻ đỏ, khóa tài khoản!
            if (failedAttempts >= 5) {
                user.setStatus(UserStatus.LOCKED);
                user.setOtpCode(null); // Xóa OTP để không cho dò tiếp
                userRepository.save(user);
                throw new RuntimeException("Bạn đã nhập sai OTP 5 lần. Tài khoản đã bị khóa để bảo mật!");
            }

            userRepository.save(user);
            throw new RuntimeException("Mã OTP không chính xác! Bạn còn " + (5 - failedAttempts) + " lần thử.");
        }

        // 3. NẾU ĐÚNG: Xóa án tích (Reset số lần sai) và Xóa OTP
        user.setOtpFailedAttempts(0);
        user.setOtpCode(null);
        user.setOtpExpiryTime(null);
        userRepository.save(user);

        // 4. Mọi thứ hoàn hảo -> Đúc chiếc "Thẻ Sửa Chữa"
        String resetToken = jwtService.generateResetToken(user);

        return VerifyResetOtpResponse.builder()
                .resetToken(resetToken)
                .message("Xác thực thành công. Vui lòng tiến hành đặt lại mật khẩu!")
                .build();
    }

    @Override
    public void resetPassword(String token, ResetPasswordRequest request) {
        // CHỐT CHẶN BỔ SUNG: Kiểm tra xem thẻ này đã bị thu hồi chưa!
        if (invalidatedTokenRepository.existsById(token)) {
            throw new RuntimeException("Token khôi phục này đã được sử dụng hoặc không còn hợp lệ!");
        }

        // 1. Kiểm tra con dấu: Tuyệt đối không cho dùng Access Token để đổi mật khẩu!
        String purpose = jwtService.extractPurpose(token);
        if (!"RESET_PASSWORD".equals(purpose)) {
            throw new RuntimeException("Token không hợp lệ! Vui lòng thực hiện lại luồng Quên mật khẩu.");
        }

        // 2. Giải mã lấy Email và tìm người dùng
        String email = jwtService.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));

        // 3. Mã hóa Mật khẩu mới và lưu xuống DB
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 4. HỦY THẺ NGAY LẬP TỨC (Cho vào Danh sách đen) đảm bảo thẻ Reset chỉ được dùng ĐÚNG 1 LẦN!
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(token)
                .expiryTime(jwtService.extractExpiration(token))
                .build();
        invalidatedTokenRepository.save(invalidatedToken);

        log.info("Đã đổi mật khẩu thành công và thu hồi Reset Token cho email: {}", email);
    }

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