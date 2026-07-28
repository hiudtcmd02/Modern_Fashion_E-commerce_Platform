package com.dth.fashionshop.modules.promotion.dto.request;

import com.dth.fashionshop.modules.promotion.enums.DiscountType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class VoucherRequest {

    @Schema(description = "Mã Voucher", example = "SUMMER26")
    @NotBlank(message = "Mã voucher không được để trống")
    @Size(max = 50, message = "Mã voucher không được vượt quá 50 ký tự")
    private String code;

    @Schema(description = "Tên chương trình", example = "Giảm giá mùa hè")
    @NotBlank(message = "Tên chương trình không được để trống")
    @Size(max = 100, message = "Tên chương trình không được vượt quá 100 ký tự")
    private String name;

    @Schema(description = "Mô tả chi tiết thể lệ của voucher")
    private String description;

    @Schema(description = "Loại giảm giá (PERCENT (Giảm theo %) hoặc FIXED_AMOUNT (Giảm số tiền cố định đơn vị VNĐ))", example = "FIXED_AMOUNT")
    @NotNull(message = "Vui lòng chọn loại giảm giá")
    private DiscountType discountType;

    @Schema(description = "Mức giảm (Nếu là PERCENT thì lớn hơn 0 và bé hơn hoặc bằng 100, Nếu là FIXED_AMOUNT thì là số tiền)", example = "50000")
    @NotNull(message = "Vui lòng nhập mức giảm")
    @Min(value = 1, message = "Mức giảm phải lớn hơn 0")
    private Long discountValue;

    @Schema(description = "Số tiền giảm tối đa (Chỉ áp dụng khi loại giảm giá là PERCENT, truyền vào giá trị NULL nếu không giới hạn)", example = "100000")
    @Min(value = 1, message = "Số tiền giảm tối đa phải lớn hơn 0")
    private Long maxDiscountAmount;

    @Schema(description = "Giá trị đơn hàng tối thiểu", example = "300000")
    @NotNull(message = "Vui lòng nhập giá trị đơn hàng tối thiểu")
    @Min(value = 0, message = "Giá trị đơn hàng tối thiểu không được là số âm")
    private Long minOrderValue;

    @Schema(description = "Cờ đánh dấu Voucher không giới hạn số lượt sử dụng (true: Không giới hạn, false: Có giới hạn)", example = "false")
    @NotNull(message = "Vui lòng xác định Voucher có giới hạn số lượt sử dụng hay không?")
    private Boolean isUnlimited;

    @Schema(description = "Tổng số lượt sử dụng (Truyền vào giá trị NULL nếu is_unlimited = true)", example = "100")
    @Min(value = 1, message = "Tổng số lượt sử dụng phải lớn hơn 0")
    private Integer usageLimit;

    @Schema(description = "Thời gian bắt đầu", example = "2026-03-10 00:00:00")
    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    @Schema(description = "Thời gian kết thúc", example = "2026-03-20 23:59:59")
    @NotNull(message = "Thời gian kết thúc không được để trống")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
}