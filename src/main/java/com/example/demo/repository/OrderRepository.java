package com.example.demo.repository;

import com.example.demo.enums.OrderStatus;
import com.example.demo.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Long> {

    Optional<Orders> findByReferenceNumber(String referenceNumber);
    Optional<Orders> findByClientCodeAndReferenceNumber (Long clientCode, String referenceNumber);
    List<Orders> findByOrderStatus(OrderStatus orderStatus);
    List<Orders> findByRiderId (Long staffId);
    List<Orders> findAllByClientCodeAndCreatedAtBetween(Long clientCode, LocalDate startDate, LocalDate stopDate);
    List<Orders> findAllByCreatedAt (LocalDate localDate);
    List<Orders> findAllByCreatedAtBetween (LocalDate startDate, LocalDate stopDate);
    List<Orders> findByClientCode (Long clientCode);

}
