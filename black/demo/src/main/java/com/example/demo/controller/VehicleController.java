package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.Vehicle;
import com.example.demo.repository.VehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "http://localhost:5173")
public class VehicleController {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // 获取所有车辆
    @GetMapping
    public ResponseEntity<ApiResponse<List<Vehicle>>> getAllVehicles() {
        try {
            List<Vehicle> vehicles = vehicleRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success(vehicles));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取车辆列表失败: " + e.getMessage()));
        }
    }

    // 创建新车辆
    @PostMapping
    public ResponseEntity<ApiResponse<Vehicle>> createVehicle(@RequestBody Vehicle vehicle) {
        try {
            System.out.println("Received vehicle request: " + objectMapper.writeValueAsString(vehicle));

            // 检查必填字段
            if (vehicle.getPlateNumber() == null || vehicle.getPlateNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("车牌号不能为空"));
            }

            // 检查车牌号是否已存在
            if (vehicleRepository.existsByPlateNumber(vehicle.getPlateNumber())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("车牌号已存在"));
            }
            
            // 设置默认值
            if (vehicle.getOwnerType() == null || vehicle.getOwnerType().trim().isEmpty()) {
                vehicle.setOwnerType("业主");
            }
            if (vehicle.getStatus() == null || vehicle.getStatus().trim().isEmpty()) {
                vehicle.setStatus("正常");
            }
            if (vehicle.getVehicleType() == null || vehicle.getVehicleType().trim().isEmpty()) {
                vehicle.setVehicleType("小型车");
            }
            
            // 设置创建时间
            if (vehicle.getCreatedAt() == null) {
                vehicle.setCreatedAt(new Date());
            }
            
            System.out.println("Saving vehicle: " + objectMapper.writeValueAsString(vehicle));
            Vehicle savedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(ApiResponse.success(savedVehicle));
        } catch (Exception e) {
            System.err.println("Error creating vehicle: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建车辆信息失败: " + e.getMessage()));
        }
    }

    // 更新车辆信息
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Vehicle>> updateVehicle(
            @PathVariable Long id, 
            @RequestBody Vehicle vehicle) {
        try {
            Vehicle existingVehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("车辆不存在"));

            // 更新停车位信息
            if (vehicle.getParkingSpace() != null) {
                existingVehicle.setParkingSpace(vehicle.getParkingSpace());
            }

            Vehicle updatedVehicle = vehicleRepository.save(existingVehicle);
            return ResponseEntity.ok(ApiResponse.success(updatedVehicle));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("更新车辆信息失败: " + e.getMessage()));
        }
    }

    // 删除车辆
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(@PathVariable Long id) {
        try {
            if (!vehicleRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            vehicleRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("删除车辆失败: " + e.getMessage()));
        }
    }

    // 搜索车辆
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Vehicle>>> searchVehicles(@RequestParam String query) {
        List<Vehicle> vehicles = vehicleRepository.findByPlateNumberContaining(query);
        return ResponseEntity.ok(ApiResponse.success(vehicles));
    }
} 