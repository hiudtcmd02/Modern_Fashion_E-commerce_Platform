package com.dth.fashionshop.modules.identity.service;

import com.dth.fashionshop.modules.identity.entity.User;
import com.dth.fashionshop.modules.identity.enums.UserStatus;
import com.dth.fashionshop.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        boolean isEnabled = user.getStatus() != UserStatus.INACTIVE;

        boolean isAccountNonLocked = user.getStatus() != UserStatus.LOCKED;

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                isEnabled,
                true,
                true,
                isAccountNonLocked,
                authorities
        );
    }
}