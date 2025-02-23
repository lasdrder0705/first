package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.MaintenanceRequest;
import com.example.demo.dto.MaintenanceHandleRequest;
import com.example.demo.entity.Maintenance;
import com.example.demo.repository.MaintenanceRepository;
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
import java.util.*;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin(origins = "http://localhost:5173")
public class MaintenanceController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // 提交报修
    @PostMapping
    public ResponseEntity<ApiResponse<Maintenance>> submitMaintenance(
            @ModelAttribute MaintenanceRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证必填字段
            if (request.getType() == null || request.getType().trim().isEmpty() ||
                request.getUrgency() == null || request.getUrgency().trim().isEmpty() ||
                request.getBuildingId() == null ||
                request.getLocation() == null || request.getLocation().trim().isEmpty() ||
                request.getDescription() == null || request.getDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("请填写所有必填项"));
            }

            // 获取当前用户ID
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            Long userId = getUserIdFromUsername(username);  // 需要实现此方法

            // 处理图片上传
            List<String> imageUrls = new ArrayList<>();
            if (request.getImages() != null && !request.getImages().isEmpty()) {
                for (MultipartFile file : request.getImages()) {
                    String fileName = UUID.randomUUID().toString() + 
                        getFileExtension(file.getOriginalFilename());
                    Path filePath = Paths.get(uploadDir, fileName);
                    Files.copy(file.getInputStream(), filePath);
                    imageUrls.add("/api/uploads/" + fileName);
                }
            }

            // 创建报修记录
            Maintenance maintenance = new Maintenance();
            maintenance.setType(request.getType().trim());
            maintenance.setUrgency(request.getUrgency().trim());
            maintenance.setBuildingId(request.getBuildingId());
            maintenance.setLocation(request.getLocation().trim());
            maintenance.setDescription(request.getDescription().trim());
            maintenance.setImageUrls(objectMapper.writeValueAsString(imageUrls));
            maintenance.setCreatedBy(userId);
            maintenance.setCreatedAt(new Date());
            maintenance.setStatus("待处理");

            Maintenance savedMaintenance = maintenanceRepository.save(maintenance);
            return ResponseEntity.ok(ApiResponse.success(savedMaintenance));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("提交报修失败: " + e.getMessage()));
        }
    }

    // 获取我的报修记录
    @GetMapping("/my-records")
    public ResponseEntity<ApiResponse<List<Maintenance>>> getMyRecords(
            @RequestHeader("Authorization") String token) {
        try {
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            Long userId = getUserIdFromUsername(username);
            List<Maintenance> records = maintenanceRepository.findByCreatedByOrderByCreatedAtDesc(userId);
            return ResponseEntity.ok(ApiResponse.success(records));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取报修记录失败: " + e.getMessage()));
        }
    }

    // 获取待处理的维修请求列表
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Maintenance>>> getPendingMaintenance() {
        try {
            List<Maintenance> pendingList = maintenanceRepository.findByStatusOrderByUrgencyDescCreatedAtAsc("待处理");
            return ResponseEntity.ok(ApiResponse.success(pendingList));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取待处理维修列表失败: " + e.getMessage()));
        }
    }

    // 开始处理维修请求
    @PutMapping("/{id}/start")
    public ResponseEntity<ApiResponse<Maintenance>> startMaintenance(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            Long userId = getUserIdFromUsername(username);

            Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("维修请求不存在"));

            if (!"待处理".equals(maintenance.getStatus())) {
                throw new RuntimeException("该维修请求已在处理中或已完成");
            }

            maintenance.setStatus("处理中");
            maintenance.setHandledBy(userId);
            Maintenance updatedMaintenance = maintenanceRepository.save(maintenance);

            return ResponseEntity.ok(ApiResponse.success(updatedMaintenance));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("开始处理维修失败: " + e.getMessage()));
        }
    }

    // 完成维修请求
    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Maintenance>> completeMaintenance(
            @PathVariable Long id,
            @RequestBody MaintenanceHandleRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            Long userId = getUserIdFromUsername(username);

            Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("维修请求不存在"));

            if (!"处理中".equals(maintenance.getStatus())) {
                throw new RuntimeException("该维修请求状态异常");
            }

            maintenance.setStatus("已完成");
            maintenance.setResult(request.getResult());
            maintenance.setHandledAt(new Date());
            Maintenance updatedMaintenance = maintenanceRepository.save(maintenance);

            return ResponseEntity.ok(ApiResponse.success(updatedMaintenance));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("完成维修失败: " + e.getMessage()));
        }
    }

    // 获取维修人员的维修历史记录
    @GetMapping("/my-handled")
    public ResponseEntity<ApiResponse<List<Maintenance>>> getMyHandledMaintenance(
            @RequestHeader("Authorization") String token) {
        try {
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            Long userId = getUserIdFromUsername(username);

            List<Maintenance> handledList = maintenanceRepository.findByHandledByOrderByHandledAtDesc(userId);
            return ResponseEntity.ok(ApiResponse.success(handledList));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取维修历史记录失败: " + e.getMessage()));
        }
    }

    // 获取文件扩展名
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
} 