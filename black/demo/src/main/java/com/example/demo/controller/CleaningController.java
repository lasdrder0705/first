package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CleaningTaskRequest;
import com.example.demo.entity.CleaningLog;
import com.example.demo.entity.CleaningTask;
import com.example.demo.entity.User;
import com.example.demo.repository.CleaningLogRepository;
import com.example.demo.repository.CleaningTaskRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CleaningTaskService;
import com.example.demo.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class CleaningController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private CleaningTaskRepository taskRepository;

    @Autowired
    private CleaningLogRepository logRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CleaningTaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    // 获取今日清洁任务
    @GetMapping("/cleaning-tasks/today")
    public ResponseEntity<ApiResponse<List<CleaningTask>>> getTodayTasks() {
        try {
            List<CleaningTask> tasks = taskService.getTodayTasks();
            return ResponseEntity.ok(ApiResponse.success(tasks));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取清洁任务失败: " + e.getMessage()));
        }
    }

    // 开始清洁
    @PutMapping("/cleaning-tasks/{taskId}/start")
    public ResponseEntity<ApiResponse<CleaningTask>> startCleaning(
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String token) {
        try {
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            
            CleaningTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

            if (!"pending".equals(task.getStatus())) {
                throw new RuntimeException("该任务已经开始或已完成");
            }

            task.setStatus("ongoing");
            CleaningTask updatedTask = taskRepository.save(task);

            return ResponseEntity.ok(ApiResponse.success(updatedTask));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("开始清洁失败: " + e.getMessage()));
        }
    }

    // 提交清洁记录
    @PostMapping("/cleaning-logs")
    public ResponseEntity<ApiResponse<CleaningLog>> submitCleaningLog(
            @RequestParam("taskId") Long taskId,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestParam("content") String content,
            @RequestParam("condition") String condition,
            @RequestParam(value = "conditionDesc", required = false) String conditionDesc,
            @RequestParam("hasDamage") Boolean hasDamage,
            @RequestParam(value = "damageDesc", required = false) String damageDesc,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestHeader("Authorization") String token) {
        try {
            // 获取当前用户信息
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 获取任务信息
            CleaningTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

            // 处理图片上传
            List<String> imageUrls = new ArrayList<>();
            if (images != null && !images.isEmpty()) {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                for (MultipartFile file : images) {
                    if (!file.isEmpty()) {
                        String fileName = UUID.randomUUID().toString() + 
                            getFileExtension(file.getOriginalFilename());
                        Path filePath = uploadPath.resolve(fileName);
                        Files.copy(file.getInputStream(), filePath);
                        imageUrls.add("/api/uploads/" + fileName);
                    }
                }
            }

            // 创建清洁记录
            CleaningLog log = new CleaningLog();
            log.setTaskId(taskId);
            log.setCleanerId(user.getId());
            log.setCleanerName(user.getNickname());
            log.setTimeSlot(task.getTimeSlot());
            log.setArea(task.getArea());
            log.setStartTime(new Date(Long.parseLong(startTime)));
            log.setEndTime(new Date(Long.parseLong(endTime)));
            log.setContent(content);
            log.setHygieneCondition(condition);
            log.setConditionDesc(conditionDesc);
            log.setHasDamage(hasDamage);
            log.setDamageDesc(damageDesc);
            log.setImageUrls(objectMapper.writeValueAsString(imageUrls));

            // 更新任务状态
            task.setStatus("completed");
            taskRepository.save(task);

            // 保存清洁记录
            CleaningLog savedLog = logRepository.save(log);
            return ResponseEntity.ok(ApiResponse.success(savedLog));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("提交清洁记录失败: " + e.getMessage()));
        }
    }

    // 获取清洁记录
    @GetMapping("/cleaning-logs")
    public ResponseEntity<ApiResponse<List<CleaningLog>>> getCleaningLogs(
            @RequestHeader("Authorization") String token) {
        try {
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            List<CleaningLog> logs = logRepository.findByCleanerIdOrderByStartTimeDesc(user.getId());
            return ResponseEntity.ok(ApiResponse.success(logs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取清洁记录失败: " + e.getMessage()));
        }
    }

    // 获取清洁异常记录
    @GetMapping("/cleaning-logs/abnormal")
    public ResponseEntity<ApiResponse<List<CleaningLog>>> getAbnormalLogs() {
        try {
            List<CleaningLog> abnormalLogs = logRepository.findByHasDamageOrHygieneCondition(
                true, "poor");
            return ResponseEntity.ok(ApiResponse.success(abnormalLogs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取清洁异常记录失败: " + e.getMessage()));
        }
    }

    // 开始处理清洁异常
    @PutMapping("/cleaning-logs/{id}/start-repair")
    public ResponseEntity<ApiResponse<CleaningLog>> startRepair(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            CleaningLog log = logRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

            log.setStatus("处理中");
            CleaningLog updatedLog = logRepository.save(log);
            return ResponseEntity.ok(ApiResponse.success(updatedLog));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("开始处理失败: " + e.getMessage()));
        }
    }

    // 完成清洁异常处理
    @PutMapping("/cleaning-logs/{id}/complete-repair")
    public ResponseEntity<ApiResponse<CleaningLog>> completeRepair(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            CleaningLog log = logRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

            log.setStatus("已完成");
            log.setRepairResult(request.get("result"));
            log.setRepairTime(new Date());
            
            CleaningLog updatedLog = logRepository.save(log);
            return ResponseEntity.ok(ApiResponse.success(updatedLog));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("完成处理失败: " + e.getMessage()));
        }
    }

    // 创建清洁任务
    @PostMapping("/cleaning-tasks")
    public ResponseEntity<ApiResponse<CleaningTask>> createCleaningTask(
            @RequestBody CleaningTaskRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证是否是管理员
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            if (!"admin".equals(user.getRole())) {
                throw new RuntimeException("只有管理员可以创建任务");
            }

            // 验证必填字段
            if (request.getTimeSlot() == null || request.getTimeSlot().trim().isEmpty()) {
                throw new RuntimeException("时间段不能为空");
            }
            if (request.getArea() == null || request.getArea().trim().isEmpty()) {
                throw new RuntimeException("清洁区域不能为空");
            }

            // 创建新任务
            CleaningTask task = new CleaningTask();
            task.setTimeSlot(request.getTimeSlot().trim());
            task.setArea(request.getArea().trim());
            task.setFocus(request.getFocus());
            task.setAssignedTo(request.getAssignedTo());
            task.setStatus("pending");
            task.setCreatedAt(new Date());

            CleaningTask savedTask = taskRepository.save(task);
            return ResponseEntity.ok(ApiResponse.success(savedTask));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("创建任务失败: " + e.getMessage()));
        }
    }

    private String getFileExtension(String filename) {
        return Optional.ofNullable(filename)
            .filter(f -> f.contains("."))
            .map(f -> f.substring(f.lastIndexOf(".")))
            .orElse(".jpg");
    }
} 