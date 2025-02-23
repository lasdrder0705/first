package com.example.demo.service;

import com.example.demo.entity.CleaningTask;
import com.example.demo.repository.CleaningTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class CleaningTaskService {

    @Autowired
    private CleaningTaskRepository taskRepository;

    @Scheduled(cron = "0 0 1 * * ?")
    public void createDailyCleaningTasks() {
        Date today = new Date();
        List<CleaningTask> existingTasks = taskRepository.findTodayTasks(today);
        if (!existingTasks.isEmpty()) {
            return;
        }

        List<CleaningTask> dailyTasks = Arrays.asList(
            createTask("早班 (06:00-14:00)", "公共区域", "重点清洁电梯和大堂"),
            createTask("中班 (14:00-22:00)", "休闲区域", "保持环境整洁"),
            createTask("晚班 (22:00-06:00)", "办公区域", "清理垃圾桶和地面")
        );

        taskRepository.saveAll(dailyTasks);
    }

    private CleaningTask createTask(String timeSlot, String area, String focus) {
        CleaningTask task = new CleaningTask();
        task.setTimeSlot(timeSlot);
        task.setArea(area);
        task.setFocus(focus);
        task.setStatus("pending");
        task.setCreatedAt(new Date());
        return task;
    }

    public List<CleaningTask> getTodayTasks() {
        Date today = new Date();
        List<CleaningTask> tasks = taskRepository.findTodayTasks(today);
        if (tasks.isEmpty()) {
            createDailyCleaningTasks();
            tasks = taskRepository.findTodayTasks(today);
        }
        return tasks;
    }
} 