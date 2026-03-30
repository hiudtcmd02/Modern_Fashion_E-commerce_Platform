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

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        if (identityService.isTokenInvalidated(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        userEmail = jwtService.extractEmail(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (userService.isUserLocked(userEmail)) {
                logger.warn("Phát hiện tài khoản bị khóa đang cố truy cập: " + userEmail);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Tài khoản của bạn đã bị khóa bởi Quản trị viên!");
                return;
            }

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}