package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.ParkingSpace;
import com.example.demo.repository.ParkingSpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parking-spaces")
@CrossOrigin(origins = "http://localhost:5173")
public class ParkingSpaceController {

    @Autowired
    private ParkingSpaceRepository parkingSpaceRepository;

    // 获取所有车位
    @GetMapping
    public ResponseEntity<ApiResponse<List<ParkingSpace>>> getAllParkingSpaces() {
        try {
            List<ParkingSpace> spaces = parkingSpaceRepository.findAll();
            
            // 确保每个车位对象的 building 字段都被正确处理
            spaces.forEach(space -> {
                if (space.getBuilding() == null) {
                    space.setBuilding(null); // 确保 null 值被正确处理
                }
            });
            
            return ResponseEntity.ok(ApiResponse.success(spaces));
        } catch (Exception e) {
            e.printStackTrace(); // 添加日志输出以便调试
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取车位列表失败: " + e.getMessage()));
        }
    }

    // 创建新车位
    @PostMapping
    public ResponseEntity<ApiResponse<ParkingSpace>> createParkingSpace(@RequestBody ParkingSpace parkingSpace) {
        try {
            // 验证必填字段
            if (parkingSpace.getSpaceNumber() == null || parkingSpace.getBuilding() == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("车位编号和所属楼房不能为空"));
            }

            // 将 building 字符串转换为 Long
            Long buildingId;
            try {
                buildingId = Long.parseLong(parkingSpace.getBuilding());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("楼房ID格式不正确"));
            }

            // 检查同一楼房内是否存在相同车位编号
            boolean exists = parkingSpaceRepository.existsByBuildingAndSpaceNumber(
                buildingId, 
                parkingSpace.getSpaceNumber()
            );

            if (exists) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("该楼房内已存在相同车位编号"));
            }

            // 设置默认值
            if (parkingSpace.getStatus() == null) {
                parkingSpace.setStatus("空闲");
            }

            // 设置正确的 building ID
            parkingSpace.setBuilding(buildingId);

            ParkingSpace savedSpace = parkingSpaceRepository.save(parkingSpace);
            return ResponseEntity.ok(ApiResponse.success(savedSpace));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("创建车位失败: " + e.getMessage()));
        }
    }

    // 更新车位信息
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ParkingSpace>> updateParkingSpace(
            @PathVariable Long id, 
            @RequestBody ParkingSpace parkingSpace) {
        try {
            // 检查车位是否存在
            if (!parkingSpaceRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            // 获取原车位信息
            ParkingSpace existingSpace = parkingSpaceRepository.findById(id).get();
            
            // 保留原有状态和一些不可修改的信息
            parkingSpace.setId(id);
            parkingSpace.setStatus(existingSpace.getStatus());
            parkingSpace.setRentalStart(existingSpace.getRentalStart());
            parkingSpace.setRentalEnd(existingSpace.getRentalEnd());

            // 保存更新
            ParkingSpace updatedSpace = parkingSpaceRepository.save(parkingSpace);
            return ResponseEntity.ok(ApiResponse.success(updatedSpace));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("更新车位信息失败: " + e.getMessage()));
        }
    }

    // 删除车位
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteParkingSpace(@PathVariable Long id) {
        try {
            // 检查车位是否存在
            ParkingSpace parkingSpace = parkingSpaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("车位不存在"));

            // 检查车位状态，只有空闲状态的车位可以删除
            if (!"空闲".equals(parkingSpace.getStatus())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("只能删除空闲状态的车位"));
            }

            parkingSpaceRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("删除车位失败: " + e.getMessage()));
        }
    }

    // 搜索车位
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ParkingSpace>>> searchParkingSpaces(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status) {
        try {
            List<ParkingSpace> spaces;
            if (status != null && !status.isEmpty()) {
                spaces = parkingSpaceRepository.findByStatus(status);
            } else if (query != null && !query.isEmpty()) {
                spaces = parkingSpaceRepository.findBySpaceNumberContaining(query);
            } else {
                spaces = parkingSpaceRepository.findAll();
            }
            return ResponseEntity.ok(ApiResponse.success(spaces));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("搜索车位失败: " + e.getMessage()));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<ParkingSpace>>> getAvailableParkingSpaces() {
        try {
            // 只获取状态为"空闲"的车位
            List<ParkingSpace> availableSpaces = parkingSpaceRepository.findByStatus("空闲");
            return ResponseEntity.ok(ApiResponse.success(availableSpaces));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取空闲车位失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{spaceNumber}/status")
    public ResponseEntity<ApiResponse<ParkingSpace>> updateParkingSpaceStatus(
        @PathVariable String spaceNumber,
        @RequestBody Map<String, String> request) {
        try {
            ParkingSpace space = parkingSpaceRepository.findBySpaceNumber(spaceNumber)
                .orElseThrow(() -> new RuntimeException("车位不存在"));
            
            space.setStatus(request.get("status"));
            space.setOwnerName(request.get("ownerName"));
            
            ParkingSpace updatedSpace = parkingSpaceRepository.save(space);
            return ResponseEntity.ok(ApiResponse.success(updatedSpace));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("更新车位状态失败: " + e.getMessage()));
        }
    }
} 