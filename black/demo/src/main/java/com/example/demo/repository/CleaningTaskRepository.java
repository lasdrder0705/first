package com.example.demo.repository;

import com.example.demo.entity.CleaningTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Date;
import java.util.List;

public interface CleaningTaskRepository extends JpaRepository<CleaningTask, Long> {
    @Query("SELECT t FROM CleaningTask t WHERE DATE(t.createdAt) = DATE(:date)")
    List<CleaningTask> findTodayTasks(Date date);
} 