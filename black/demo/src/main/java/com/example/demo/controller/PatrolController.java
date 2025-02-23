package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PatrolLogRequest;
import com.example.demo.dto.PatrolTaskRequest;
import com.example.demo.entity.PatrolLog;
import com.example.demo.entity.PatrolTask;
import com.example.demo.entity.User;
import com.example.demo.repository.PatrolLogRepository;
import com.example.demo.repository.PatrolTaskRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.PatrolTaskService;
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
public class PatrolController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private PatrolTaskRepository taskRepository;

    @Autowired
    private PatrolLogRepository logRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatrolTaskService patrolTaskService;

    // 获取今日巡逻任务
    @GetMapping("/patrol-tasks/today")
    public ResponseEntity<ApiResponse<List<PatrolTask>>> getTodayTasks(
            @RequestHeader("Authorization") String token) {
        try {
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            Long userId = getUserIdFromUsername(username);
            List<PatrolTask> tasks = patrolTaskService.getTodayTasks();
            return ResponseEntity.ok(ApiResponse.success(tasks));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取巡逻任务失败: " + e.getMessage()));
        }
    }

    // 提交巡逻记录
    @PostMapping("/patrol-logs")
    public ResponseEntity<ApiResponse<PatrolLog>> submitPatrolLog(
            @ModelAttribute PatrolLogRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // 打印完整的请求信息
            System.out.println("Complete request details:");
            System.out.println("Request: " + objectMapper.writeValueAsString(request));
            System.out.println("Token: " + token);

            // 打印详细的请求信息
            System.out.println("Received patrol log request:");
            System.out.println("TaskId: " + request.getTaskId());
            System.out.println("StartTime: " + request.getStartTime());
            System.out.println("EndTime: " + request.getEndTime());
            System.out.println("Route: " + request.getRoute());
            System.out.println("HasAbnormal: " + request.getHasAbnormal());
            System.out.println("AbnormalDesc: " + request.getAbnormalDesc());
            System.out.println("Images: " + (request.getImages() != null ? request.getImages().size() : 0));

            // 验证必填字段
            if (request.getTaskId() == null ||
                request.getStartTime() == null ||
                request.getEndTime() == null ||
                request.getRoute() == null || request.getRoute().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("请填写所有必填项"));
            }

            // 获取当前用户信息
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            Long userId = getUserIdFromUsername(username);
            String userName = getUserNameFromUsername(username);

            // 获取任务信息
            PatrolTask task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("任务不存在"));

            // 处理图片上传
            List<String> imageUrls = new ArrayList<>();
            if (request.getImages() != null && !request.getImages().isEmpty()) {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                for (MultipartFile file : request.getImages()) {
                    if (!file.isEmpty()) {
                        String fileName = UUID.randomUUID().toString() + 
                            getFileExtension(file.getOriginalFilename());
                        Path filePath = uploadPath.resolve(fileName);
                        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                        imageUrls.add("/api/uploads/" + fileName);
                    }
                }
            }

            // 创建巡逻记录
            PatrolLog log = new PatrolLog();
            log.setTaskId(request.getTaskId());
            log.setPatrollerId(userId);
            log.setPatrollerName(userName);
            log.setTimeSlot(task.getTimeSlot());
            log.setArea(task.getArea());
            log.setStartTime(request.getStartTime());
            log.setEndTime(request.getEndTime());
            log.setRoute(request.getRoute().trim());
            log.setHasAbnormal(request.getHasAbnormal() != null ? request.getHasAbnormal() : false);
            
            if (Boolean.TRUE.equals(request.getHasAbnormal()) && request.getAbnormalDesc() != null) {
                log.setAbnormalDesc(request.getAbnormalDesc().trim());
            }
            
            log.setImageUrls(objectMapper.writeValueAsString(imageUrls));

            // 更新任务状态
            task.setStatus("completed");
            taskRepository.save(task);

            // 保存巡逻记录
            PatrolLog savedLog = logRepository.save(log);
            
            // 打印保存结果
            System.out.println("Saved patrol log: " + savedLog.getId());
            
            return ResponseEntity.ok(ApiResponse.success(savedLog));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error details: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("提交巡逻记录失败: " + e.getMessage()));
        }
    }

    // 获取巡逻记录
    @GetMapping("/patrol-logs")
    public ResponseEntity<ApiResponse<List<PatrolLog>>> getPatrolLogs(
            @RequestHeader("Authorization") String token) {
        try {
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            Long userId = getUserIdFromUsername(username);
            List<PatrolLog> logs = logRepository.findByPatrollerIdOrderByStartTimeDesc(userId);
            return ResponseEntity.ok(ApiResponse.success(logs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取巡逻记录失败: " + e.getMessage()));
        }
    }

    // 管理员手动创建巡逻任务
    @PostMapping("/patrol-tasks")
    public ResponseEntity<ApiResponse<PatrolTask>> createPatrolTask(
            @RequestBody PatrolTaskRequest request,
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
                throw new RuntimeException("巡逻区域不能为空");
            }

            // 创建新任务
            PatrolTask task = new PatrolTask();
            task.setTimeSlot(request.getTimeSlot().trim());
            task.setArea(request.getArea().trim());
            task.setFocus(request.getFocus());
            task.setAssignedTo(request.getAssignedTo());
            task.setStatus("pending");
            task.setCreatedAt(new Date());

            PatrolTask createdTask = taskRepository.save(task);
            return ResponseEntity.ok(ApiResponse.success(createdTask));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("创建巡逻任务失败: " + e.getMessage()));
        }
    }

    // 分配巡逻任务
    @PutMapping("/patrol-tasks/{taskId}/assign")
    public ResponseEntity<ApiResponse<PatrolTask>> assignPatrolTask(
            @PathVariable Long taskId,
            @RequestParam Long securityGuardId,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证是否是管理员
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            // TODO: 验证用户是否是管理员

            PatrolTask assignedTask = patrolTaskService.assignTask(taskId, securityGuardId);
            return ResponseEntity.ok(ApiResponse.success(assignedTask));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("分配巡逻任务失败: " + e.getMessage()));
        }
    }

    // 开始巡逻
    @PutMapping("/patrol-tasks/{taskId}/start")
    public ResponseEntity<ApiResponse<PatrolTask>> startPatrol(
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String token) {
        try {
            // 获取当前用户信息
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            Long userId = getUserIdFromUsername(username);

            // 获取任务信息
            PatrolTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

            // 验证任务状态
            if (!"pending".equals(task.getStatus())) {
                throw new RuntimeException("该任务已经开始或已完成");
            }

            // 更新任务状态
            task.setStatus("ongoing");
            task.setAssignedTo(userId);
            PatrolTask updatedTask = taskRepository.save(task);

            return ResponseEntity.ok(ApiResponse.success(updatedTask));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("开始巡逻失败: " + e.getMessage()));
        }
    }

    // 取消巡逻
    @PutMapping("/patrol-tasks/{taskId}/cancel")
    public ResponseEntity<ApiResponse<PatrolTask>> cancelPatrol(
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String token) {
        try {
            // 获取当前用户信息
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            Long userId = getUserIdFromUsername(username);

            // 获取任务信息
            PatrolTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

            // 验证任务状态
            if (!"ongoing".equals(task.getStatus())) {
                throw new RuntimeException("该任务不是巡逻中状态");
            }

            // 更新任务状态
            task.setStatus("pending");
            task.setAssignedTo(null); // 清除分配信息
            PatrolTask updatedTask = taskRepository.save(task);

            return ResponseEntity.ok(ApiResponse.success(updatedTask));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("取消巡逻失败: " + e.getMessage()));
        }
    }

    // 获取进行中的巡逻记录
    @GetMapping("/patrol-tasks/{taskId}/current-record")
    public ResponseEntity<ApiResponse<PatrolLog>> getCurrentRecord(
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String token) {
        try {
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            Long userId = getUserIdFromUsername(username);

            // 获取任务信息
            PatrolTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

            // 验证任务状态
            if (!"ongoing".equals(task.getStatus())) {
                throw new RuntimeException("该任务不是巡逻中状态");
            }

            // 获取进行中的巡逻记录
            PatrolLog currentLog = logRepository.findByTaskId(taskId)
                .orElse(null);

            return ResponseEntity.ok(ApiResponse.success(currentLog));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取巡逻记录失败: " + e.getMessage()));
        }
    }

    // 辅助方法
    private String getFileExtension(String filename) {
        return Optional.ofNullable(filename)
            .filter(f -> f.contains("."))
            .map(f -> f.substring(f.lastIndexOf(".")))
            .orElse(".jpg");
    }

    // 从用户名获取用户ID（需要实现）
    private Long getUserIdFromUsername(String username) {
        // 实现从用户名获取用户ID的逻辑
        return 1L; // 临时返回
    }

    // 从用户名获取用户姓名（需要实现）
    private String getUserNameFromUsername(String username) {
        // 实现从用户名获取用户姓名的逻辑
        return "张三"; // 临时返回
    }
} 