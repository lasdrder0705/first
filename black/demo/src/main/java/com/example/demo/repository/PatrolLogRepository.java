package com.example.demo.repository;

import com.example.demo.entity.PatrolLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PatrolLogRepository extends JpaRepository<PatrolLog, Long> {
    Optional<PatrolLog> findByTaskId(Long taskId);
    List<PatrolLog> findByPatrollerIdOrderByStartTimeDesc(Long patrollerId);
} 