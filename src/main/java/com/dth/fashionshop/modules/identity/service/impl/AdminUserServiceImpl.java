package com.dth.fashionshop.modules.identity.service.impl;

import com.dth.fashionshop.modules.identity.dto.response.AddressResponse;
import com.dth.fashionshop.modules.identity.dto.response.UserAdminResponse;
import com.dth.fashionshop.modules.identity.dto.response.UserDetailAdminResponse;
import com.dth.fashionshop.modules.identity.entity.Role;
import com.dth.fashionshop.modules.identity.entity.User;
import com.dth.fashionshop.modules.identity.enums.UserStatus;
import com.dth.fashionshop.modules.identity.repository.AddressRepository;
import com.dth.fashionshop.modules.identity.repository.UserRepository;
import com.dth.fashionshop.modules.identity.service.AdminUserService;
import com.dth.fashionshop.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

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

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<User> userPage = userRepository.searchAndFilterUsers(keyword, status, pageable);

        return userPage.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng này!"));

        boolean isTargetAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().contains("ROLE_ADMIN"));

        if (isTargetAdmin) {
            throw new RuntimeException("Hành động bị từ chối: Không được phép khóa tài khoản của Quản trị viên khác!");
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
            log.info("Admin đã MỞ KHÓA cho tài khoản: {}", user.getEmail());
        } else {
            user.setStatus(UserStatus.LOCKED);
            log.info("Admin đã KHÓA tài khoản: {}", user.getEmail());
        }

        userRepository.save(user);
    }

    @Override
    public UserDetailAdminResponse getUserDetailById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng này!"));

        List<AddressResponse> addresses = addressRepository
                .findByUserAndIsDeletedFalseOrderByIsDefaultDescCreatedAtDesc(user)
                .stream()
                .map(address -> AddressResponse.builder()
                        .id(address.getId())
                        .receiverName(address.getReceiverName())
                        .receiverPhone(address.getReceiverPhone())
                        .city(address.getCity())
                        .district(address.getDistrict())
                        .ward(address.getWard())
                        .street(address.getStreet())
                        .isDefault(address.getIsDefault())
                        .build())
                .collect(Collectors.toList());

        // (TODO) - Gọi sang OrderRepository để lấy 5 đơn hàng gần nhất
        // VD: List<Order> orders = orderRepository.findTop5ByUserOrderByCreatedAtDesc(user);

        return UserDetailAdminResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .addresses(addresses)
                .build();
    }
}