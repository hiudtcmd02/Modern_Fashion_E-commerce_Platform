package com.dth.fashionshop.modules.identity.repository;

import com.dth.fashionshop.modules.identity.entity.Address;
import com.dth.fashionshop.modules.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserAndIsDeletedFalseOrderByIsDefaultDescCreatedAtDesc(User user);

    Optional<Address> findByIdAndUserAndIsDeletedFalse(Long id, User user);

    Optional<Address> findByUserAndIsDefaultTrueAndIsDeletedFalse(User user);

    long countByUserAndIsDeletedFalse(User user);
}