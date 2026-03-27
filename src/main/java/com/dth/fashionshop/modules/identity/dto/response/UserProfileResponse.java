package com.dth.fashionshop.modules.identity.dto.response;

import com.dth.fashionshop.modules.identity.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserProfileResponse {
    private String email;
    private String fullName;
    private String phoneNumber;
    private Gender gender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private String avatarUrl;
}