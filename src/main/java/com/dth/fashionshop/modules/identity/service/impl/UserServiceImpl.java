package com.dth.fashionshop.modules.identity.service.impl;

import com.dth.fashionshop.modules.identity.dto.request.UpdateProfileRequest;
import com.dth.fashionshop.modules.identity.dto.response.UserProfileResponse;
import com.dth.fashionshop.modules.identity.entity.User;
import com.dth.fashionshop.modules.identity.repository.UserRepository;
import com.dth.fashionshop.modules.identity.service.MediaService;
import com.dth.fashionshop.modules.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MediaService mediaService;

    // Hàm dùng chung: Lấy User đang đăng nhập từ Thẻ JWT
    private User getCurrentAuthenticatedUser() {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tài khoản!"));
    }

    // Hàm chuyển Entity thành DTO
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

        // Cập nhật các trường được phép
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

        // 1. Nhờ MediaService đẩy ảnh lên Cloudinary
        String avatarUrl = mediaService.uploadAvatar(file);

        // 2. Lưu Link ảnh vào Database
        user.setAvatarUrl(avatarUrl);
        User updatedUser = userRepository.save(user);

        log.info("Người dùng {} đã cập nhật ảnh đại diện", user.getEmail());

        // Trả về toàn bộ profile mới nhất để Frontend đồng bộ UI ngay lập tức
        return mapToResponse(updatedUser);
    }
}