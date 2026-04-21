package com.dth.fashionshop.modules.identity.service.impl;

import com.dth.fashionshop.modules.identity.dto.request.UpdateProfileRequest;
import com.dth.fashionshop.modules.identity.dto.response.UserProfileResponse;
import com.dth.fashionshop.modules.identity.entity.User;
import com.dth.fashionshop.modules.identity.enums.UserStatus;
import com.dth.fashionshop.modules.identity.repository.UserRepository;
import com.dth.fashionshop.modules.identity.service.JwtService;
import com.dth.fashionshop.shared.exception.ResourceNotFoundException;
import com.dth.fashionshop.shared.media.MediaService;
import com.dth.fashionshop.modules.identity.service.UserService;
import com.dth.fashionshop.shared.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.dth.fashionshop.modules.identity.dto.request.ChangePasswordRequest;
import com.dth.fashionshop.modules.identity.entity.InvalidatedToken;
import com.dth.fashionshop.modules.identity.repository.InvalidatedTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MediaService mediaService;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Override
    public User getCurrentAuthenticatedUser() {
        String email = SecurityUtils.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản!"));
    }

    private UserProfileResponse mapToResponse(User user) {
        return UserProfileResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Override
    public UserProfileResponse getMyProfile() {
        User user = getCurrentAuthenticatedUser();
        return mapToResponse(user);
    }

    @Override
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentAuthenticatedUser();

        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());

        User updatedUser = userRepository.save(user);
        log.info("Người dùng {} đã cập nhật hồ sơ thành công", user.getEmail());

        return mapToResponse(updatedUser);
    }

    @Override
    public UserProfileResponse uploadAvatar(MultipartFile file) {
        User user = getCurrentAuthenticatedUser();

        if (user.getAvatarUrl() != null) {
            mediaService.deleteImage(user.getAvatarUrl());
        }

        String avatarUrl = mediaService.uploadImage(file, "fashionshop/avatars");

        user.setAvatarUrl(avatarUrl);
        User updatedUser = userRepository.save(user);

        log.info("Người dùng {} đã cập nhật ảnh đại diện", user.getEmail());

        return mapToResponse(updatedUser);
    }

    @Override
    public void changePassword(String token, ChangePasswordRequest request) {

        User user = getCurrentAuthenticatedUser();

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu mới và nhập lại mật khẩu mới không trùng khớp!");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác!");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu mới phải khác mật khẩu hiện tại!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(token)
                .expiryTime(jwtService.extractExpiration(token))
                .build();
        invalidatedTokenRepository.save(invalidatedToken);

        log.info("Người dùng {} đã đổi mật khẩu thành công và phiên đăng nhập cũ đã bị hủy.", user.getEmail());
    }

    @Override
    public boolean isUserLocked(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getStatus() == UserStatus.LOCKED)
                .orElse(false);
    }
}