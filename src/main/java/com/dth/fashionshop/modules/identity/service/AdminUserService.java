package com.dth.fashionshop.modules.identity.service;

import com.dth.fashionshop.modules.identity.dto.response.UserAdminResponse;
import com.dth.fashionshop.modules.identity.enums.UserStatus;
import org.springframework.data.domain.Page;

public interface AdminUserService {
    // Trả về đối tượng Page của Spring Boot (chứa sẵn totalElements, totalPages cho Frontend)
    Page<UserAdminResponse> getAllUsers(String keyword, UserStatus status, int page, int size);

    void toggleUserStatus(Long userId);
}