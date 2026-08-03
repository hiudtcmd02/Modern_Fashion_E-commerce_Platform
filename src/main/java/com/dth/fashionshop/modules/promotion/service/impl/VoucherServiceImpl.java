package com.dth.fashionshop.modules.promotion.service.impl;

import com.dth.fashionshop.modules.order.service.AdminOrderService;
import com.dth.fashionshop.modules.promotion.dto.request.VoucherApplyRequest;
import com.dth.fashionshop.modules.promotion.dto.request.VoucherRequest;
import com.dth.fashionshop.modules.promotion.dto.response.VoucherApplyResponse;
import com.dth.fashionshop.modules.promotion.dto.response.VoucherGuestResponse;
import com.dth.fashionshop.modules.promotion.dto.response.VoucherResponse;
import com.dth.fashionshop.modules.promotion.entity.Voucher;
import com.dth.fashionshop.modules.promotion.enums.DiscountType;
import com.dth.fashionshop.modules.promotion.enums.VoucherTab;
import com.dth.fashionshop.modules.promotion.repository.VoucherRepository;
import com.dth.fashionshop.modules.promotion.service.VoucherService;
import com.dth.fashionshop.shared.exception.ResourceNotFoundException;
import com.dth.fashionshop.shared.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final AdminOrderService adminOrderService;

    private VoucherResponse mapToResponse(Voucher voucher) {
        Long revenue = adminOrderService.calculateRevenueByVoucherCode(voucher.getCode());

        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .name(voucher.getName())
                .description(voucher.getDescription())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .minOrderValue(voucher.getMinOrderValue())
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .isActive(voucher.getIsActive())
                .totalRevenue(revenue)
                .build();
    }

    private VoucherGuestResponse mapToGuestResponse(Voucher voucher) {
        return VoucherGuestResponse.builder()
                .code(voucher.getCode())
                .name(voucher.getName())
                .description(voucher.getDescription())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .minOrderValue(voucher.getMinOrderValue())
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherResponse> getAllVouchers(String keyword, VoucherTab tab, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        LocalDateTime now = LocalDateTime.now();

        Page<Voucher> voucherPage = voucherRepository.searchAndFilterAdminVouchers(
                keyword,
                tab != null ? tab.name() : VoucherTab.ALL.name(),
                now,
                pageable
        );

        return voucherPage.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherResponse getVoucherById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Voucher với ID: " + id));
        return mapToResponse(voucher);
    }

    @Override
    @Transactional
    public VoucherResponse createVoucher(VoucherRequest request) {
        validateVoucherDates(request.getStartDate(), request.getEndDate(), true);
        validateDiscountLogic(request);

        String normalizedCode = StringUtils.normalizeCode(request.getCode());
        if (voucherRepository.existsByCode(normalizedCode)) {
            throw new RuntimeException("Mã Voucher đã tồn tại. Vui lòng nhập mã khác!");
        }

        if (!request.getIsUnlimited() && request.getUsageLimit() == null) {
            throw new RuntimeException("Vui lòng nhập tổng số lượt sử dụng hoặc chọn Không giới hạn số lượt!");
        }
        Integer finalUsageLimit = request.getIsUnlimited() ? null : request.getUsageLimit();

        Voucher voucher = Voucher.builder()
                .code(normalizedCode)
                .name(request.getName())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderValue(request.getMinOrderValue())
                .usageLimit(finalUsageLimit)
                .usedCount(0)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(true)
                .build();

        Voucher savedVoucher = voucherRepository.save(voucher);
        log.info("Admin đã tạo Voucher mới: {}", savedVoucher.getCode());
        return mapToResponse(savedVoucher);
    }

    @Override
    @Transactional
    public VoucherResponse updateVoucher(Long id, VoucherRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Voucher với ID: " + id));

        LocalDateTime now = LocalDateTime.now();

        if (!voucher.getIsActive() || now.isAfter(voucher.getEndDate())) {
            throw new RuntimeException("Voucher này đã kết thúc, dữ liệu bị khóa để bảo vệ báo cáo. Không thể cập nhật!");
        }

        validateVoucherDates(request.getStartDate(), request.getEndDate(), false);
        validateDiscountLogic(request);

        if (!request.getIsUnlimited() && request.getUsageLimit() == null) {
            throw new RuntimeException("Vui lòng nhập tổng số lượt sử dụng hoặc chọn Không giới hạn số lượt!");
        }
        Integer finalUsageLimit = request.getIsUnlimited() ? null : request.getUsageLimit();

        boolean isOngoing = voucher.getIsActive() && now.isAfter(voucher.getStartDate()) && now.isBefore(voucher.getEndDate());
        boolean isUpcoming = voucher.getIsActive() && now.isBefore(voucher.getStartDate());

        if (isUpcoming && request.getStartDate().isBefore(now.minusMinutes(5))) {
            throw new RuntimeException("Voucher chưa bắt đầu. Không thể cập nhật Ngày bắt đầu lùi về quá khứ!");
        }

        if (isOngoing) {
            if (!voucher.getStartDate().isEqual(request.getStartDate())) {
                throw new RuntimeException("Voucher đang diễn ra. Không thể thay đổi Ngày bắt đầu!");
            }

            if (!Objects.equals(voucher.getUsageLimit(), finalUsageLimit)) {
                throw new RuntimeException("Voucher đang diễn ra. Không thể thay đổi giới hạn số lượt sử dụng!");
            }
        }

        if (isOngoing && voucher.getUsedCount() > 0) {
            boolean isRuleChanged = voucher.getDiscountType() != request.getDiscountType()
                    || !voucher.getDiscountValue().equals(request.getDiscountValue())
                    || (voucher.getMaxDiscountAmount() != null && !voucher.getMaxDiscountAmount().equals(request.getMaxDiscountAmount()))
                    || (voucher.getMaxDiscountAmount() == null && request.getMaxDiscountAmount() != null)
                    || !voucher.getMinOrderValue().equals(request.getMinOrderValue());

            if (isRuleChanged) {
                throw new RuntimeException("Voucher đang diễn ra và đã có người sử dụng. Không thể thay đổi Loại giảm giá, Mức giảm, Giảm tối đa và Giá trị đơn tối thiểu!");
            }
        }

        if (finalUsageLimit != null && finalUsageLimit < voucher.getUsedCount()) {
            throw new RuntimeException("Tổng số lượt sử dụng (" + finalUsageLimit +
                    ") không được nhỏ hơn số lượt đã có người dùng (" + voucher.getUsedCount() + ")!");
        }

        voucher.setName(request.getName());
        voucher.setDescription(request.getDescription());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setUsageLimit(finalUsageLimit);
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());

        Voucher updatedVoucher = voucherRepository.save(voucher);
        log.info("Admin đã cập nhật Voucher: {}", updatedVoucher.getCode());
        return mapToResponse(updatedVoucher);
    }

    @Override
    @Transactional
    public void endVoucherEarly(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Voucher với ID: " + id));

        LocalDateTime now = LocalDateTime.now();
        if (!voucher.getIsActive() || now.isAfter(voucher.getEndDate())) {
            throw new RuntimeException("Voucher này đã ở trạng thái kết thúc!");
        }

        if (now.isBefore(voucher.getStartDate())) {
            voucher.setStartDate(now);
        }

        voucher.setEndDate(now);
        voucher.setIsActive(false);

        voucherRepository.save(voucher);
        log.info("Admin đã kết thúc sớm Voucher: {}", voucher.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherGuestResponse> getStorefrontVouchers(String keyword, VoucherTab tab, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        LocalDateTime now = LocalDateTime.now();

        Page<Voucher> voucherPage = voucherRepository.searchStorefrontVouchers(
                keyword,
                tab != null ? tab.name() : VoucherTab.ALL.name(),
                now,
                pageable
        );

        return voucherPage.map(this::mapToGuestResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherGuestResponse getVoucherDetailByCode(String code) {
        String normalizedCode = StringUtils.normalizeCode(code);
        Voucher voucher = voucherRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new ResourceNotFoundException("Mã khuyến mãi không tồn tại."));

        return mapToGuestResponse(voucher);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherApplyResponse calculateDiscount(VoucherApplyRequest request) {
        String normalizedCode = StringUtils.normalizeCode(request.getCode());
        Voucher voucher = voucherRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new RuntimeException("Mã khuyến mãi không tồn tại."));

        validateVoucherForUsage(voucher, request.getCartTotal());

        Long discountAmount = calculateDiscountAmount(voucher, request.getCartTotal());

        return VoucherApplyResponse.builder()
                .code(voucher.getCode())
                .discountAmount(discountAmount)
                .build();
    }

    @Override
    @Transactional
    public void useVoucherWithLock(String code, Long cartTotal) {
        String normalizedCode = StringUtils.normalizeCode(code);

        Voucher voucher = voucherRepository.findByCodeWithLock(normalizedCode)
                .orElseThrow(() -> new RuntimeException("Mã khuyến mãi không tồn tại."));

        validateVoucherForUsage(voucher, cartTotal);

        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);

        log.info("Đã trừ 1 lượt sử dụng của Voucher: {}. Số lượt đã dùng: {}", voucher.getCode(), voucher.getUsedCount());
    }

    @Override
    @Transactional
    public void refundVoucherUsageWithLock(String code) {
        String normalizedCode = StringUtils.normalizeCode(code);

        Voucher voucher = voucherRepository.findByCodeWithLock(normalizedCode).orElse(null);
        if (voucher != null && voucher.getUsedCount() > 0) {
            voucher.setUsedCount(voucher.getUsedCount() - 1);
            voucherRepository.save(voucher);
            log.info("Hoàn trả 1 lượt sử dụng cho Voucher do hủy đơn: {}. Lượt đã dùng hiện tại: {}", voucher.getCode(), voucher.getUsedCount());
        }
    }

    private void validateVoucherDates(LocalDateTime startDate, LocalDateTime endDate, boolean isCreate) {
        LocalDateTime now = LocalDateTime.now();

        if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu!");
        }

        if (endDate.isBefore(now)) {
            throw new RuntimeException("Ngày kết thúc phải lớn hơn thời gian hiện tại!");
        }

        if (isCreate && startDate.isBefore(now.minusMinutes(5))) {
            throw new RuntimeException("Ngày bắt đầu không được nằm trong quá khứ!");
        }
    }

    private void validateDiscountLogic(VoucherRequest request) {
        if (request.getDiscountType() == DiscountType.PERCENT) {
            if (request.getDiscountValue() > 100) {
                throw new RuntimeException("Mức giảm giá theo phần trăm không được vượt quá 100%!");
            }
        } else if (request.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            if (request.getMaxDiscountAmount() != null) {
                throw new RuntimeException("Giảm giá theo số tiền cố định không được phép thiết lập số tiền giảm tối đa!");
            }
        }
    }

    private void validateVoucherForUsage(Voucher voucher, Long cartTotal) {
        LocalDateTime now = LocalDateTime.now();

        if (!voucher.getIsActive() || now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            throw new RuntimeException("Mã khuyến mãi đã hết hạn hoặc chưa bắt đầu.");
        }

        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new RuntimeException("Mã khuyến mãi đã hết lượt sử dụng.");
        }

        if (cartTotal < voucher.getMinOrderValue()) {
            DecimalFormat formatter = new DecimalFormat("#,###");
            String minOrderFormatted = formatter.format(voucher.getMinOrderValue());
            throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu (" + minOrderFormatted + " VNĐ) để áp dụng mã này.");
        }
    }

    private Long calculateDiscountAmount(Voucher voucher, Long cartTotal) {
        long discountAmount = 0L;

        if (voucher.getDiscountType() == DiscountType.PERCENT) {
            discountAmount = (cartTotal * voucher.getDiscountValue()) / 100;
            if (voucher.getMaxDiscountAmount() != null && discountAmount > voucher.getMaxDiscountAmount()) {
                discountAmount = voucher.getMaxDiscountAmount();
            }
        } else if (voucher.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discountAmount = voucher.getDiscountValue();
        }

        return Math.min(discountAmount, cartTotal);
    }
}