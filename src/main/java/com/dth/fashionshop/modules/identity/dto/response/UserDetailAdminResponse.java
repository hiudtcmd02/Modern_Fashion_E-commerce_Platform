package com.dth.fashionshop.modules.identity.dto.response;

import com.dth.fashionshop.modules.identity.enums.Gender;
import com.dth.fashionshop.modules.identity.enums.UserStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserDetailAdminResponse {

    // Thông tin cá nhân Profile
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private UserStatus status;
    private LocalDateTime createdAt;
    private List<String> roles;

    // Sổ địa chỉ
    private List<AddressResponse> addresses;
}