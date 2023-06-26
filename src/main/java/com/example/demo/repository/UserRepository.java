package com.example.demo.repository;

import com.example.demo.enums.RiderStatus;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<User> findByConfirmationToken(String token);

    Optional<User> findByStaffId(Long staffId);

    Optional<User> findByEmailOrStaffId(String email, Long staffId);
    User findByRiderStatus (RiderStatus riderStatus);
}
