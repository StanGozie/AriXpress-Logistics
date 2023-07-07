package com.example.demo.repository;

import com.example.demo.enums.RiderStatus;
import com.example.demo.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByEmail(String email);
    Boolean existsByEmail(String email);
    Optional<Staff> findByConfirmationToken(String token);
    Optional<Staff> findByStaffId(Long staffId);
    List<Staff> findByRiderStatus (RiderStatus riderStatus);

    Optional<Staff> findByEmailOrStaffId(String email, Long staffId);


}
