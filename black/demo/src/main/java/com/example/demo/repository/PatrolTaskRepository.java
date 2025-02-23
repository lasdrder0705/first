package com.example.demo.repository;

import com.example.demo.entity.PatrolTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Date;
import java.util.List;

public interface PatrolTaskRepository extends JpaRepository<PatrolTask, Long> {
    @Query("SELECT t FROM PatrolTask t WHERE DATE(t.createdAt) = DATE(:date)")
    List<PatrolTask> findTodayTasks(Date date);
    
    @Query("SELECT t FROM PatrolTask t WHERE DATE(t.createdAt) = DATE(:date) AND t.assignedTo = :userId")
    List<PatrolTask> findTodayTasksByUser(Date date, Long userId);

    List<PatrolTask> findByAssignedToAndStatus(Long userId, String status);
} 