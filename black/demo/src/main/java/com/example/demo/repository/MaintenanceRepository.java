package com.example.demo.repository;

import com.example.demo.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {
    List<Maintenance> findByCreatedByOrderByCreatedAtDesc(Long userId);
    List<Maintenance> findByStatusOrderByUrgencyDescCreatedAtAsc(String status);
    List<Maintenance> findByHandledByOrderByHandledAtDesc(Long handledBy);
} 