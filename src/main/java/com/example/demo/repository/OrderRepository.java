package com.example.demo.repository;

import com.example.demo.enums.OrderStatus;
import com.example.demo.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Long> {

    Optional<Orders> findById(Long id);
    Optional<Orders> findByEmailAndId(String email, Long id);
    List<Orders> findByOrderStatus(OrderStatus orderStatus);
    List<Orders> findByRiderId (Long staffId);
    Optional<Orders> findByClientIdAndCreatedAt(Long clientId, LocalDate startDate);
    Optional<Orders> findByClientId (Long id);
    Optional<Orders> findByOrderDate(LocalDate localDate);


}
