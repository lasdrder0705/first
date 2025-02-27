package com.example.demo.repository;

import com.example.demo.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findByNameContaining(String name);
} 