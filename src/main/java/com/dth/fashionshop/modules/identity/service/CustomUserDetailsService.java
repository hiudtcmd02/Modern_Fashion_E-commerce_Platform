package com.dth.fashionshop.modules.identity.service;

import com.dth.fashionshop.modules.identity.entity.User;
import com.dth.fashionshop.modules.identity.enums.UserStatus;
import com.dth.fashionshop.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Tìm User trong Database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        // 2. Lấy đúng 1 Quyền duy nhất (Giống hệt cách làm ở JwtService)
        String roleName = user.getRoles().iterator().next().getName();

        // 3. Đóng gói quyền đó vào 1 cái List duy nhất
        var authorities = Collections.singletonList(new SimpleGrantedAuthority(roleName));

        // Nếu status KHÁC INACTIVE nghĩa là đã kích hoạt (true)
        boolean isEnabled = user.getStatus() != UserStatus.INACTIVE;

        // Nếu status KHÁC LOCKED nghĩa là không bị khóa (true)
        boolean isAccountNonLocked = user.getStatus() != UserStatus.LOCKED;

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                isEnabled,          // Dành cho INACTIVE (Bắn ra DisabledException)
                true,               // accountNonExpired
                true,               // credentialsNonExpired
                isAccountNonLocked, // Dành cho LOCKED (Bắn ra LockedException)
                authorities
        );
    }
}