package com.dth.fashionshop.modules.identity.repository;

import com.dth.fashionshop.modules.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // SELECT count(*) FROM users WHERE email = ?
    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}