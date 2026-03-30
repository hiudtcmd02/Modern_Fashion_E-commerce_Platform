package com.dth.fashionshop.modules.identity.service;

import com.dth.fashionshop.modules.identity.dto.response.UserAdminResponse;
import com.dth.fashionshop.modules.identity.dto.response.UserDetailAdminResponse;
import com.dth.fashionshop.modules.identity.enums.UserStatus;
import org.springframework.data.domain.Page;

public interface AdminUserService {

    Page<UserAdminResponse> getAllUsers(String keyword, UserStatus status, int page, int size);

    void toggleUserStatus(Long userId);

    UserDetailAdminResponse getUserDetailById(Long id);
}