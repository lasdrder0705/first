package com.example.demo.repository;

import com.example.demo.entity.CleaningLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CleaningLogRepository extends JpaRepository<CleaningLog, Long> {
    Optional<CleaningLog> findByTaskId(Long taskId);
    List<CleaningLog> findByCleanerIdOrderByStartTimeDesc(Long cleanerId);
    List<CleaningLog> findByHasDamageOrHygieneCondition(Boolean hasDamage, String hygieneCondition);
} 