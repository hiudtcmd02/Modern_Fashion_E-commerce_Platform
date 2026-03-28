package com.dth.fashionshop.modules.identity.repository;

import com.dth.fashionshop.modules.identity.entity.Address;
import com.dth.fashionshop.modules.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // 1. Lấy danh sách địa chỉ (Chưa bị xóa), sắp xếp: Mặc định lên đầu, sau đó đến Mới nhất
    List<Address> findByUserAndIsDeletedFalseOrderByIsDefaultDescCreatedAtDesc(User user);

    // 2. Tìm một địa chỉ cụ thể của User (Phải đảm bảo chưa bị xóa)
    Optional<Address> findByIdAndUserAndIsDeletedFalse(Long id, User user);

    // 3. Tìm địa chỉ đang được set Mặc định của User
    Optional<Address> findByUserAndIsDefaultTrueAndIsDeletedFalse(User user);

    // 4. Đếm xem User này đang có bao nhiêu địa chỉ (Chưa bị xóa)
    long countByUserAndIsDeletedFalse(User user);
}