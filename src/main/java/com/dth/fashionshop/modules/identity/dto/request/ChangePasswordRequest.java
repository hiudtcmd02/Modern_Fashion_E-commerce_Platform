package com.dth.fashionshop.modules.identity.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChangePasswordRequest {

    @Schema(description = "Mật khẩu hiện tại")
    @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại")
    private String oldPassword;

    @Schema(description = "Mật khẩu mới")
    @NotBlank(message = "Vui lòng nhập mật khẩu mới")
    @Size(min = 6, message = "Mật khẩu mới phải dài ít nhất 6 ký tự")
    private String newPassword;

    @Schema(description = "Nhập lại mật khẩu mới")
    @NotBlank(message = "Vui lòng xác nhận mật khẩu mới")
    private String confirmPassword;
}