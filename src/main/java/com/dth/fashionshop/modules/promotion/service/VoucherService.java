package com.dth.fashionshop.modules.promotion.service;

import com.dth.fashionshop.modules.promotion.dto.request.VoucherApplyRequest;
import com.dth.fashionshop.modules.promotion.dto.request.VoucherRequest;
import com.dth.fashionshop.modules.promotion.dto.response.VoucherApplyResponse;
import com.dth.fashionshop.modules.promotion.dto.response.VoucherGuestResponse;
import com.dth.fashionshop.modules.promotion.dto.response.VoucherResponse;
import com.dth.fashionshop.modules.promotion.enums.VoucherTab;
import org.springframework.data.domain.Page;

public interface VoucherService {

    Page<VoucherResponse> getAllVouchers(String keyword, VoucherTab tab, int page, int size);

    VoucherResponse getVoucherById(Long id);

    VoucherResponse createVoucher(VoucherRequest request);

    VoucherResponse updateVoucher(Long id, VoucherRequest request);

    void endVoucherEarly(Long id);

    Page<VoucherGuestResponse> getStorefrontVouchers(String keyword, VoucherTab tab, int page, int size);

    VoucherGuestResponse getVoucherDetailByCode(String code);

    VoucherApplyResponse calculateDiscount(VoucherApplyRequest request);

    void useVoucherWithLock(String code, Long cartTotal);

    void refundVoucherUsageWithLock(String code);
}