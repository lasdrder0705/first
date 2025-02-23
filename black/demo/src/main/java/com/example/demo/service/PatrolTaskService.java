package com.example.demo.service;

import com.example.demo.entity.PatrolTask;
import com.example.demo.repository.PatrolTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class PatrolTaskService {

    @Autowired
    private PatrolTaskRepository taskRepository;

    // 修改为每天凌晨1点自动创建第二天的巡逻任务
    @Scheduled(cron = "0 0 1 * * ?")
    public void createDailyPatrolTasks() {
        // 检查今天是否已创建任务
        Date today = new Date();
        List<PatrolTask> existingTasks = taskRepository.findTodayTasks(today);
        if (!existingTasks.isEmpty()) {
            return; // 如果已经创建了今天的任务，就不再创建
        }

        // 定义固定的巡逻时段和区域
        List<PatrolTask> dailyTasks = Arrays.asList(
            createTask("早班 (06:00-14:00)", "A区域", "重点检查消防通道、安全出口"),
            createTask("中班 (14:00-22:00)", "B区域", "加强地下车库巡查"),
            createTask("晚班 (22:00-06:00)", "C区域", "注意各出入口安全")
        );

        // 保存任务
        taskRepository.saveAll(dailyTasks);
    }

    // 添加手动初始化今日任务的方法
    public void initializeTodayTasks() {
        Date today = new Date();
        List<PatrolTask> existingTasks = taskRepository.findTodayTasks(today);
        if (existingTasks.isEmpty()) {
            List<PatrolTask> dailyTasks = Arrays.asList(
                createTask("早班 (06:00-14:00)", "A区域", "重点检查消防通道、安全出口"),
                createTask("中班 (14:00-22:00)", "B区域", "加强地下车库巡查"),
                createTask("晚班 (22:00-06:00)", "C区域", "注意各出入口安全")
            );
            taskRepository.saveAll(dailyTasks);
        }
    }

    // 修改查询方法
    public List<PatrolTask> getTodayTasks() {
        Date today = new Date();
        List<PatrolTask> tasks = taskRepository.findTodayTasks(today);
        if (tasks.isEmpty()) {
            // 如果没有今日任务，自动初始化
            initializeTodayTasks();
            tasks = taskRepository.findTodayTasks(today);
        }
        return tasks;
    }

    // 创建单个巡逻任务
    private PatrolTask createTask(String timeSlot, String area, String focus) {
        PatrolTask task = new PatrolTask();
        task.setTimeSlot(timeSlot);
        task.setArea(area);
        task.setFocus(focus);
        task.setStatus("pending");
        task.setCreatedAt(new Date());
        return task;
    }

    // 手动创建巡逻任务
    public PatrolTask createManualTask(PatrolTask task) {
        task.setCreatedAt(new Date());
        task.setStatus("pending");
        return taskRepository.save(task);
    }

    // 分配任务给保安
    public PatrolTask assignTask(Long taskId, Long securityGuardId) {
        PatrolTask task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("任务不存在"));
        task.setAssignedTo(securityGuardId);
        return taskRepository.save(task);
    }
} 