package com.dth.fashionshop.shared.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    // Hàm gửi OTP cho chức năng đăng ký tài khoản
    @Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            log.info("Bắt đầu tiến trình gửi Email OTP đến: {}", toEmail);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Mã xác thực đăng ký tài khoản - Modern Fashion Shop");
            message.setText("Chào bạn,\n\n" +
                    "Mã xác thực (OTP) để kích hoạt tài khoản của bạn là: " + otpCode + "\n\n" +
                    "Mã này sẽ hết hạn sau 5 phút. Vui lòng KHÔNG chia sẻ mã này cho bất kỳ ai.\n\n" +
                    "Trân trọng,\n" +
                    "Đội ngũ Modern Fashion Shop.");

            javaMailSender.send(message);

            log.info("Đã gửi Email OTP thành công đến: {}", toEmail);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email đến {}: {}", toEmail, e.getMessage());
        }
    }

    // Hàm gửi otp cho chức năng quên mật khẩu
    @Async
    public void sendForgotPasswordEmail(String toEmail, String otpCode) {
        try {
            log.info("Bắt đầu tiến trình gửi Email OTP đến: {}", toEmail);

            String subject = "Yêu cầu đặt lại mật khẩu - Modern Fashion Shop";
            String text = "Xin chào,\n\n"
                    + "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.\n"
                    + "Mã OTP để xác thực của bạn là: " + otpCode + "\n\n"
                    + "Mã này sẽ hết hạn sau 5 phút. Vui lòng KHÔNG chia sẻ mã này cho bất kỳ ai.\n\n"
                    + "Trân trọng,\n"
                    + "Đội ngũ Modern Fashion Shop";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(text);

            javaMailSender.send(message);

            log.info("Đã gửi Email OTP thành công đến: {}", toEmail);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email đến {}: {}", toEmail, e.getMessage());
        }
    }
}