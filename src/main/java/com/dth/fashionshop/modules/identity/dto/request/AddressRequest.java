package com.dth.fashionshop.modules.identity.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AddressRequest {

    @Schema(description = "Tên người nhận")
    @NotBlank(message = "Tên người nhận không được để trống")
    private String receiverName;

    @Schema(description = "Số điện thoại người nhận", example = "0338542155")
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Số điện thoại không hợp lệ")
    private String receiverPhone;

    @Schema(description = "Tỉnh/Thành phố")
    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String city;

    @Schema(description = "Quận/Huyện")
    @NotBlank(message = "Quận/Huyện không được để trống")
    private String district;

    @Schema(description = "Phường/Xã")
    @NotBlank(message = "Phường/Xã không được để trống")
    private String ward;

    @Schema(description = "Địa chỉ cụ thể (Số nhà, tên đường)")
    @NotBlank(message = "Địa chỉ cụ thể (Số nhà, tên đường) không được để trống")
    private String street;

    @Schema(description = "Trạng thái mặc định")
    private Boolean isDefault;
}