package com.dth.fashionshop.modules.identity.dto.request;

import com.dth.fashionshop.modules.identity.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Họ và Tên không được để trống")
    private String fullName;

    // Regex chuẩn cho Số điện thoại Việt Nam (Bắt đầu bằng 03, 05, 07, 08, 09 và có đúng 10 số)
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    private Gender gender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
}