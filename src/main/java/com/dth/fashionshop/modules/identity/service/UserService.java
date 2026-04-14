package com.dth.fashionshop.modules.identity.service;

import com.dth.fashionshop.modules.identity.dto.request.ChangePasswordRequest;
import com.dth.fashionshop.modules.identity.dto.request.UpdateProfileRequest;
import com.dth.fashionshop.modules.identity.dto.response.UserProfileResponse;
import com.dth.fashionshop.modules.identity.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserProfileResponse getMyProfile();

    UserProfileResponse updateProfile(UpdateProfileRequest request);

    UserProfileResponse uploadAvatar(MultipartFile file);

    void changePassword(String token, ChangePasswordRequest request);

    boolean isUserLocked(String email);

    User getCurrentAuthenticatedUser();
}
