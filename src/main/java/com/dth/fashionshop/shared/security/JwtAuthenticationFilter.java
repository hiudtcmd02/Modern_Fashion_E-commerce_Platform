package com.dth.fashionshop.shared.security;

import com.dth.fashionshop.modules.identity.service.CustomUserDetailsService;
import com.dth.fashionshop.modules.identity.service.IdentityService;
import com.dth.fashionshop.modules.identity.service.JwtService;
import com.dth.fashionshop.modules.identity.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final IdentityService identityService;
    private final UserService userService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            @Lazy IdentityService identityService,
            @Lazy UserService userService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.identityService = identityService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Lấy Tờ giấy thông hành (Header Authorization) từ Request gửi lên
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Nếu không có giấy, hoặc giấy không bắt đầu bằng chữ "Bearer " (chuẩn của JWT)
        // -> Bỏ qua không quét nữa, nhường cho các bộ lọc khác xử lý (chắc là khách vãng lai)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Cắt lấy phần mã Token (Bỏ đi 7 ký tự đầu là chữ "Bearer ")
        jwt = authHeader.substring(7);

        // Kiểm tra xem thẻ này có nằm trong Danh sách đen không?
        if (identityService.isTokenInvalidated(jwt)) {
            // Thẻ đã bị hủy! Ngưng phục vụ, đuổi ra cổng!
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Nhờ JwtService giải mã để lấy Email ra
        userEmail = jwtService.extractEmail(jwt);

        // 5. Nếu có Email và hệ thống chưa ghi nhận người này đang đăng nhập
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Truy vấn vào DB kiểm tra xem người dùng có đang bị khóa không
            if (userService.isUserLocked(userEmail)) {
                // Nếu bị khóa, ném ngay mã 401 (Unauthorized) và cắt đứt luồng chạy!
                logger.warn("Phát hiện tài khoản bị khóa đang cố truy cập: " + userEmail);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Tài khoản của bạn đã bị khóa bởi Quản trị viên!");
                return; // Dừng ngay lập tức, không cho đi tiếp vào FilterChain
            }

            // Lấy hồ sơ của người này từ Database lên
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Nhờ JwtService soi xem thẻ có khớp với hồ sơ và còn hạn không
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Thẻ hợp lệ! Cấp thẻ xanh (UsernamePasswordAuthenticationToken) cho đi qua
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Không cần pass nữa vì thẻ JWT đã chứng minh rồi
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Báo cáo với Giám đốc An ninh (SecurityContextHolder): "Người này ok, cho vào!"
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. Cho phép đi tiếp vào Controller
        filterChain.doFilter(request, response);
    }
}