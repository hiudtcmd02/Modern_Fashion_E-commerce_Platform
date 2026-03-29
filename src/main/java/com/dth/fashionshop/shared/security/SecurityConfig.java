package com.dth.fashionshop.shared.security;

import com.dth.fashionshop.modules.identity.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

    // 1. Cấu hình thuật toán mã hóa mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Thuật toán băm mật khẩu 1 chiều an toàn nhất hiện nay
    }

    // 2. Viết lại bản Nội quy gác cổng (Security Filter Chain)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF vì chúng ta làm REST API gọi từ ReactJS, không dùng Form HTML truyền thống
                .csrf(AbstractHttpConfigurer::disable)

                // Tắt Session, hệ thống sẽ không nhớ ai đang đăng nhập (Stateless) - Tiền đề để dùng JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Phân quyền chi tiết cho từng đường dẫn (URL)
                .authorizeHttpRequests(auth -> auth
                        // Mở cửa hoàn toàn cho các API của khách vãng lai
                        .requestMatchers(
                                "/api/v1/identity/login",
                                "/api/v1/identity/register",
                                "/api/v1/identity/verify-otp",
                                "/api/v1/identity/resend-otp",
                                "/api/v1/identity/forgot-password",
                                "/api/v1/identity/reset-password",
                                "/api/v1/identity/verify-reset-otp",
                                "/error"
                        ).permitAll()

                        // Mọi API khác trong hệ thống đều bị khóa, phải có Token mới được vào
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 1. Cỗ máy xác thực (Quy định cách thức kiểm tra mật khẩu)
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // TRUYỀN THẲNG userDetailsService VÀO HÀM KHỞI TẠO Ở ĐÂY:
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);

        // Vẫn cấu hình máy giải mã mật khẩu bình thường
        authProvider.setPasswordEncoder(passwordEncoder());

        // Không còn cần gọi hàm setUserDetailsService nữa!

        return authProvider;
    }

    // 2. Vị Giám đốc An ninh (Sẽ được gọi ở tầng Controller/Service để kích hoạt tiến trình đăng nhập)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}