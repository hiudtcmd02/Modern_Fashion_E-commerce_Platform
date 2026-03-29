package com.dth.fashionshop.modules.identity.service.impl;

import com.dth.fashionshop.modules.identity.dto.response.UserAdminResponse;
import com.dth.fashionshop.modules.identity.entity.Role;
import com.dth.fashionshop.modules.identity.entity.User;
import com.dth.fashionshop.modules.identity.enums.UserStatus;
import com.dth.fashionshop.modules.identity.repository.UserRepository;
import com.dth.fashionshop.modules.identity.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    // Hàm tiện ích: Biến Entity thô thành DTO sang trọng cho Admin
    private UserAdminResponse mapToResponse(User user) {
        return UserAdminResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public Page<UserAdminResponse> getAllUsers(String keyword, UserStatus status, int page, int size) {
        // 1. Tạo đối tượng Pageable (Mặc định sắp xếp Tài khoản mới nhất lên đầu)
        // Lưu ý: Frontend thường truyền page bắt đầu từ 1, nhưng Spring Boot đếm từ 0.
        // Ở Controller chúng ta sẽ trừ đi 1 sau, ở Service cứ nhận chuẩn số 0-based.
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 2. Gọi siêu vũ khí ở Repository
        Page<User> userPage = userRepository.searchAndFilterUsers(keyword, status, pageable);

        // 3. Sự kỳ diệu của Spring Page: Hàm .map() tự động duyệt qua từng User trong danh sách
        // và biến nó thành UserAdminResponse, trong khi vẫn giữ nguyên các thông số phân trang (total, size...)
        return userPage.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId) {

        // 1. Tìm người dùng trong DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng này!"));

        // 2. CHỐT CHẶN 5.1: Quét Radar xem người này có phải là Admin không?
        boolean isTargetAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().contains("ROLE_ADMIN"));

        if (isTargetAdmin) {
            throw new RuntimeException("Hành động bị từ chối: Không được phép khóa tài khoản của Quản trị viên khác!");
        }

        // 3. Thực hiện Đảo trạng thái (Toggle)
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
            log.info("Admin đã MỞ KHÓA cho tài khoản: {}", user.getEmail());
        } else {
            user.setStatus(UserStatus.LOCKED);
            log.info("Admin đã KHÓA tài khoản: {}", user.getEmail());
        }

        // 4. Lưu lại sự thay đổi
        userRepository.save(user);
    }
}