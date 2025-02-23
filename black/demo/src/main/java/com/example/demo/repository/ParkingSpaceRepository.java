package com.example.demo.repository;

import com.example.demo.entity.ParkingSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {
    List<ParkingSpace> findBySpaceNumberContaining(String query);
    List<ParkingSpace> findByStatus(String status);
    boolean existsBySpaceNumber(String spaceNumber);
    Optional<ParkingSpace> findBySpaceNumber(String spaceNumber);
    boolean existsByBuildingAndSpaceNumber(Long building, String spaceNumber);
} 