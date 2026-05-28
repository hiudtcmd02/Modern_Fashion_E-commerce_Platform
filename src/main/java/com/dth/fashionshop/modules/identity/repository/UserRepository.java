package com.dth.fashionshop.modules.identity.repository;

import com.dth.fashionshop.modules.identity.entity.User;
import com.dth.fashionshop.modules.identity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);

    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR :keyword = '' " +
            "   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR u.phoneNumber LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:status IS NULL OR u.status = :status)")
    Page<User> searchAndFilterUsers(
            @Param("keyword") String keyword,
            @Param("status") UserStatus status,
            Pageable pageable
    );
}