package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.VisitorRequest;
import com.example.demo.entity.Visitor;
import com.example.demo.repository.VisitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/visitors")
@CrossOrigin(origins = "http://localhost:5173")
public class VisitorController {

    @Autowired
    private VisitorRepository visitorRepository;

    // 获取今日访客记录
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<Visitor>>> getTodayVisitors() {
        try {
            List<Visitor> visitors = visitorRepository.findAllByVisitDate(new Date());
            return ResponseEntity.ok(ApiResponse.success(visitors));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取访客记录失败: " + e.getMessage()));
        }
    }

    // 搜索访客
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Visitor>>> searchVisitors(
            @RequestParam String query) {
        try {
            List<Visitor> visitors = visitorRepository
                .findByIdCardContainingOrPhoneContaining(query, query);
            return ResponseEntity.ok(ApiResponse.success(visitors));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("搜索访客失败: " + e.getMessage()));
        }
    }

    // 登记新访客
    @PostMapping
    public ResponseEntity<ApiResponse<Visitor>> registerVisitor(
            @RequestBody VisitorRequest request) {
        try {
            // 验证必填字段
            if (request.getName() == null || request.getName().trim().isEmpty() ||
                request.getIdCard() == null || request.getIdCard().trim().isEmpty() ||
                request.getPhone() == null || request.getPhone().trim().isEmpty() ||
                request.getVisitType() == null || request.getVisitType().trim().isEmpty() ||
                request.getPurpose() == null || request.getPurpose().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("姓名、身份证号、电话、访问类型和目的为必填项"));
            }

            // 验证身份证号格式
            String idCard = request.getIdCard().trim();
            if (!idCard.matches("(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("身份证号格式不正确"));
            }

            // 验证手机号格式
            String phone = request.getPhone().trim();
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("手机号格式不正确"));
            }

            // 创建访客记录
            Visitor visitor = new Visitor();
            visitor.setName(request.getName().trim());
            visitor.setIdCard(idCard);
            visitor.setPhone(phone);
            visitor.setVisitType(request.getVisitType().trim());
            visitor.setPurpose(request.getPurpose().trim());
            visitor.setVisitTime(new Date());
            visitor.setExpectedLeaveTime(request.getExpectedLeaveTime());
            visitor.setOwnerId(request.getOwnerId());
            visitor.setRelationWithOwner(request.getRelationWithOwner());
            visitor.setRemarks(request.getRemarks());
            visitor.setStatus("在访");

            Visitor savedVisitor = visitorRepository.save(visitor);
            return ResponseEntity.ok(ApiResponse.success(savedVisitor));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("登记访客失败: " + e.getMessage()));
        }
    }

    // 更新访客信息
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Visitor>> updateVisitor(
            @PathVariable Long id,
            @RequestBody VisitorRequest request) {
        try {
            // 验证必填字段
            if (request.getName() == null || request.getName().trim().isEmpty() ||
                request.getIdCard() == null || request.getIdCard().trim().isEmpty() ||
                request.getPhone() == null || request.getPhone().trim().isEmpty() ||
                request.getVisitType() == null || request.getVisitType().trim().isEmpty() ||
                request.getPurpose() == null || request.getPurpose().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("姓名、身份证号、电话、访问类型和目的为必填项"));
            }

            Visitor visitor = visitorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("访客不存在"));

            visitor.setName(request.getName().trim());
            visitor.setIdCard(request.getIdCard().trim());
            visitor.setPhone(request.getPhone().trim());
            visitor.setVisitType(request.getVisitType().trim());
            visitor.setPurpose(request.getPurpose().trim());
            visitor.setExpectedLeaveTime(request.getExpectedLeaveTime());
            visitor.setOwnerId(request.getOwnerId());
            visitor.setRelationWithOwner(request.getRelationWithOwner());
            visitor.setRemarks(request.getRemarks());

            Visitor updatedVisitor = visitorRepository.save(visitor);
            return ResponseEntity.ok(ApiResponse.success(updatedVisitor));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("更新访客信息失败: " + e.getMessage()));
        }
    }

    // 记录访客离开
    @PostMapping("/{id}/leave")
    public ResponseEntity<ApiResponse<Visitor>> recordVisitorLeave(
            @PathVariable Long id) {
        try {
            Visitor visitor = visitorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("访客不存在"));

            visitor.setActualLeaveTime(new Date());
            visitor.setStatus("已离开");

            Visitor updatedVisitor = visitorRepository.save(visitor);
            return ResponseEntity.ok(ApiResponse.success(updatedVisitor));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("记录访客离开失败: " + e.getMessage()));
        }
    }
} 