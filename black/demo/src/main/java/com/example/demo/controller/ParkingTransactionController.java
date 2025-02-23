package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.RentRequest;
import com.example.demo.dto.SellRequest;
import com.example.demo.entity.ParkingSpace;
import com.example.demo.repository.ParkingSpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parking-transactions")
@CrossOrigin(origins = "http://localhost:5173")
public class ParkingTransactionController {

    @Autowired
    private ParkingSpaceRepository parkingSpaceRepository;

    // 处理租赁请求
    @PostMapping("/rent")
    public ResponseEntity<ApiResponse<?>> rentParkingSpace(@RequestBody RentRequest request) {
        try {
            // 验证请求参数
            if (request.getParkingId() == null || request.getVehicleId() == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("车位ID和车辆ID不能为空"));
            }

            ParkingSpace parkingSpace = parkingSpaceRepository.findById(request.getParkingId())
                .orElseThrow(() -> new RuntimeException("车位不存在"));

            // 验证车位状态
            if (!"空闲".equals(parkingSpace.getStatus())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("该车位不是空闲状态"));
            }

            // 更新车位状态和相关信息
            parkingSpace.setStatus("已占用");
            parkingSpace.setRentalStart(request.getStartDate());
            parkingSpace.setRentalEnd(request.getEndDate());
            parkingSpaceRepository.save(parkingSpace);

            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("租赁失败: " + e.getMessage()));
        }
    }

    // 处理出售请求
    @PostMapping("/sell")
    public ResponseEntity<ApiResponse<?>> sellParkingSpace(@RequestBody SellRequest request) {
        try {
            // 验证请求参数
            if (request.getParkingId() == null || request.getVehicleId() == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("车位ID和车辆ID不能为空"));
            }

            ParkingSpace parkingSpace = parkingSpaceRepository.findById(request.getParkingId())
                .orElseThrow(() -> new RuntimeException("车位不存在"));

            // 验证车位状态
            if (!"空闲".equals(parkingSpace.getStatus())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("该车位不是空闲状态"));
            }

            // 更新车位状态和相关信息
            parkingSpace.setStatus("已售出");
            parkingSpaceRepository.save(parkingSpace);

            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("出售失败: " + e.getMessage()));
        }
    }

    // 结束租赁/停车
    @PostMapping("/{id}/end-rent")
    public ResponseEntity<ApiResponse<?>> endRent(@PathVariable Long id) {
        try {
            ParkingSpace parkingSpace = parkingSpaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("车位不存在"));

            // 验证车位状态
            if (!"已占用".equals(parkingSpace.getStatus())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("该车位不是占用状态"));
            }

            // 更新车位状态
            parkingSpace.setStatus("空闲");
            parkingSpace.setRentalStart(null);
            parkingSpace.setRentalEnd(null);
            parkingSpaceRepository.save(parkingSpace);

            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("结束租赁失败: " + e.getMessage()));
        }
    }
} 