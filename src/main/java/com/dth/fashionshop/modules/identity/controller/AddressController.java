package com.dth.fashionshop.modules.identity.controller;

import com.dth.fashionshop.modules.identity.dto.request.AddressRequest;
import com.dth.fashionshop.modules.identity.dto.response.AddressResponse;
import com.dth.fashionshop.modules.identity.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Tag(name = "Khách hàng quản lý sổ địa chỉ", description = "Các API phục vụ cho việc quản lý sổ địa chỉ của khách hàng")
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "Thêm địa chỉ mới")
    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(@Valid @RequestBody AddressRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createAddress(request));
    }

    @Operation(summary = "Khách hàng lấy danh sách sổ địa chỉ")
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getMyAddresses() {
        return ResponseEntity.ok(addressService.getMyAddresses());
    }

    @Operation(summary = "Cập nhật địa chỉ")
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(id, request));
    }

    @Operation(summary = "Xóa địa chỉ")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(Map.of("message", "Xóa địa chỉ thành công!"));
    }

    @Operation(summary = "Lấy thông tin chi tiết của một địa chỉ")
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getAddressById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getAddressById(id));
    }
}