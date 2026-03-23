package com.dth.fashionshop.shared.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j // Dùng để in log ra console
public class EmailService {

    private final JavaMailSender javaMailSender;

    // Kỹ thuật Bất đồng bộ: Hàm này sẽ chạy ngầm ở một luồng (thread) khác
    // Giúp API trả về kết quả ngay lập tức mà không bắt người dùng chờ 3-5 giây gửi mail
    @Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            log.info("Bắt đầu tiến trình gửi Email OTP đến: {}", toEmail);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Mã xác thực đăng ký tài khoản - Modern Fashion Shop");
            message.setText("Chào bạn,\n\n" +
                    "Mã xác thực (OTP) để kích hoạt tài khoản của bạn là: " + otpCode + "\n\n" +
                    "Mã này sẽ hết hạn sau 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\n" +
                    "Trân trọng,\n" +
                    "Đội ngũ Modern Fashion Shop.");

            javaMailSender.send(message);

            log.info("Đã gửi Email OTP thành công đến: {}", toEmail);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email đến {}: {}", toEmail, e.getMessage());
        }
    }
}