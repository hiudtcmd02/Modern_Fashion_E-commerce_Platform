package com.dth.fashionshop.modules.identity.service.impl;

import com.dth.fashionshop.modules.identity.dto.request.AddressRequest;
import com.dth.fashionshop.modules.identity.dto.response.AddressResponse;
import com.dth.fashionshop.modules.identity.entity.Address;
import com.dth.fashionshop.modules.identity.entity.User;
import com.dth.fashionshop.modules.identity.repository.AddressRepository;
import com.dth.fashionshop.modules.identity.service.AddressService;
import com.dth.fashionshop.modules.identity.service.UserService;
import com.dth.fashionshop.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserService userService;

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

    private void removeCurrentDefaultAddress(User user) {
        addressRepository.findByUserAndIsDefaultTrueAndIsDeletedFalse(user)
                .ifPresent(oldDefault -> {
                    oldDefault.setIsDefault(false);
                    addressRepository.save(oldDefault);
                });
    }

    @Override
    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        User user = userService.getCurrentAuthenticatedUser();

        boolean isFirstAddress = addressRepository.countByUserAndIsDeletedFalse(user) == 0;

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
                .build();

        Address createdAddress = addressRepository.save(newAddress);

        log.info("Người dùng {} đã thêm mới địa chỉ ID: {}", user.getEmail(), createdAddress.getId());

        return mapToResponse(createdAddress);
    }

    @Override
    public List<AddressResponse> getMyAddresses() {
        User user = userService.getCurrentAuthenticatedUser();

        return addressRepository.findByUserAndIsDeletedFalseOrderByIsDefaultDescCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long id, AddressRequest request) {
        User user = userService.getCurrentAuthenticatedUser();

        Address address = addressRepository.findByIdAndUserAndIsDeletedFalse(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ này!"));

        boolean wantToBeDefault = request.getIsDefault() != null && request.getIsDefault();

        if (wantToBeDefault && !address.getIsDefault()) {
            removeCurrentDefaultAddress(user);
        }

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
        User user = userService.getCurrentAuthenticatedUser();

        Address address = addressRepository.findByIdAndUserAndIsDeletedFalse(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ này!"));

        if (address.getIsDefault()) {
            throw new RuntimeException("Không thể xóa địa chỉ mặc định. Vui lòng chọn địa chỉ khác làm mặc định trước khi xóa.");
        }

        address.setIsDeleted(true);
        addressRepository.save(address);

        log.info("Người dùng {} đã xóa mềm địa chỉ ID: {}", user.getEmail(), id);
    }

    @Override
    public AddressResponse getAddressById(Long id) {
        User user = userService.getCurrentAuthenticatedUser();

        Address address = addressRepository.findByIdAndUserAndIsDeletedFalse(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ này!"));

        return mapToResponse(address);
    }
}