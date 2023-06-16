package com.example.demo.repository;

import com.example.demo.model.Bike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BikeRepository extends JpaRepository<Bike, Long> {

    Optional<Bike> findByBikeNumber(String bikeNumber);

}
