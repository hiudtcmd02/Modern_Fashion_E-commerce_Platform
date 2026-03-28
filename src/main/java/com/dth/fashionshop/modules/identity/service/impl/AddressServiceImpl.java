package com.dth.fashionshop.modules.identity.service.impl;

import com.dth.fashionshop.modules.identity.dto.request.AddressRequest;
import com.dth.fashionshop.modules.identity.dto.response.AddressResponse;
import com.dth.fashionshop.modules.identity.entity.Address;
import com.dth.fashionshop.modules.identity.entity.User;
import com.dth.fashionshop.modules.identity.repository.AddressRepository;
import com.dth.fashionshop.modules.identity.repository.UserRepository;
import com.dth.fashionshop.modules.identity.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    // Hàm tiện ích: Lấy User đang đăng nhập từ JWT
    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tài khoản!"));
    }

    // Hàm tiện ích: Chuyển Entity -> DTO
    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .city(address.getCity())
                .district(address.getDistrict())
                .ward(address.getWard())
                .street(address.getStreet())
                .isDefault(address.getIsDefault())
                .build();
    }

    // Hàm tiện ích: Tước quyền Mặc định của địa chỉ cũ
    private void removeCurrentDefaultAddress(User user) {
        addressRepository.findByUserAndIsDefaultTrueAndIsDeletedFalse(user)
                .ifPresent(oldDefault -> {
                    oldDefault.setIsDefault(false);
                    addressRepository.save(oldDefault);
                });
    }

    @Override
    @Transactional // Đảm bảo nếu lỗi giữa chừng thì Rollback toàn bộ
    public AddressResponse createAddress(AddressRequest request) {
        User user = getCurrentAuthenticatedUser();

        boolean isFirstAddress = addressRepository.countByUserAndIsDeletedFalse(user) == 0;

        // Nếu là địa chỉ đầu tiên, BẮT BUỘC phải là mặc định.
        // Nếu không, lấy theo checkbox của người dùng gửi lên.
        boolean shouldBeDefault = isFirstAddress || (request.getIsDefault() != null && request.getIsDefault());

        if (shouldBeDefault && !isFirstAddress) {
            removeCurrentDefaultAddress(user);
        }

        Address newAddress = Address.builder()
                .user(user)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .city(request.getCity())
                .district(request.getDistrict())
                .ward(request.getWard())
                .street(request.getStreet())
                .isDefault(shouldBeDefault)
                // isDeleted và createdAt/updatedAt đã được tự động xử lý
                .build();

        Address createdAddress = addressRepository.save(newAddress);

        log.info("Người dùng {} đã thêm mới địa chỉ ID: {}", user.getEmail(), createdAddress.getId());

        return mapToResponse(createdAddress);
    }

    @Override
    public List<AddressResponse> getMyAddresses() {
        User user = getCurrentAuthenticatedUser();
        // Repository đã tự động lọc isDeleted = false và sắp xếp Mặc định lên đầu
        return addressRepository.findByUserAndIsDeletedFalseOrderByIsDefaultDescCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long id, AddressRequest request) {
        User user = getCurrentAuthenticatedUser();

        // Tìm địa chỉ, đảm bảo nó là của User này và chưa bị xóa
        Address address = addressRepository.findByIdAndUserAndIsDeletedFalse(id, user)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ này!"));

        boolean wantToBeDefault = request.getIsDefault() != null && request.getIsDefault();

        // Nếu họ muốn đổi cái này thành Mặc định (trong khi trước đó nó không phải)
        if (wantToBeDefault && !address.getIsDefault()) {
            removeCurrentDefaultAddress(user);
        }

        // Nếu họ cố tình gỡ Mặc định của cái duy nhất/đang là mặc định, ta chặn lại
        // (Luôn phải có 1 cái mặc định)
        if (!wantToBeDefault && address.getIsDefault()) {
            throw new RuntimeException("Không thể gỡ bỏ trạng thái mặc định. Hãy chọn địa chỉ khác làm mặc định thay thế!");
        }

        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setWard(request.getWard());
        address.setStreet(request.getStreet());
        address.setIsDefault(wantToBeDefault || address.getIsDefault());

        Address updatedAddress = addressRepository.save(address);

        log.info("Người dùng {} đã cập nhật thông tin cho địa chỉ ID: {}", user.getEmail(), id);

        return mapToResponse(updatedAddress);
    }

    @Override
    public void deleteAddress(Long id) {
        User user = getCurrentAuthenticatedUser();

        Address address = addressRepository.findByIdAndUserAndIsDeletedFalse(id, user)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ này!"));

        // Kịch bản ngoại lệ 7.1: Không cho phép xóa địa chỉ mặc định
        if (address.getIsDefault()) {
            throw new RuntimeException("Không thể xóa địa chỉ mặc định. Vui lòng chọn địa chỉ khác làm mặc định trước khi xóa.");
        }

        // Thực hiện Xóa Mềm
        address.setIsDeleted(true);
        addressRepository.save(address);

        log.info("Người dùng {} đã xóa mềm địa chỉ ID: {}", user.getEmail(), id);
    }

    @Override
    public AddressResponse getAddressById(Long id) {
        User user = getCurrentAuthenticatedUser();

        // Tìm địa chỉ theo ID, đảm bảo là của User này và chưa bị xóa mềm
        Address address = addressRepository.findByIdAndUserAndIsDeletedFalse(id, user)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ này!"));

        return mapToResponse(address);
    }
}
