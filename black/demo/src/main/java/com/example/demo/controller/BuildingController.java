package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.Building;
import com.example.demo.repository.BuildingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buildings")
@CrossOrigin(origins = "http://localhost:5173")
public class BuildingController {

    @Autowired
    private BuildingRepository buildingRepository;

    // 获取所有楼房
    @GetMapping
    public ResponseEntity<ApiResponse<List<Building>>> getAllBuildings() {
        try {
            List<Building> buildings = buildingRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success(buildings));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取楼房列表失败: " + e.getMessage()));
        }
    }

    // 创建新楼房
    @PostMapping
    public ResponseEntity<ApiResponse<Building>> createBuilding(@RequestBody Building building) {
        try {
            Building savedBuilding = buildingRepository.save(building);
            return ResponseEntity.ok(ApiResponse.success(savedBuilding));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("创建楼房失败: " + e.getMessage()));
        }
    }

    // 更新楼房信息
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Building>> updateBuilding(@PathVariable Long id, @RequestBody Building building) {
        try {
            if (!buildingRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            building.setId(id);
            Building updatedBuilding = buildingRepository.save(building);
            return ResponseEntity.ok(ApiResponse.success(updatedBuilding));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("更新楼房失败: " + e.getMessage()));
        }
    }

    // 删除楼房
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBuilding(@PathVariable Long id) {
        try {
            if (!buildingRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            buildingRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("删除楼房失败: " + e.getMessage()));
        }
    }

    // 搜索楼房
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Building>>> searchBuildings(@RequestParam String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                // 如果查询为空，返回所有楼房
                List<Building> buildings = buildingRepository.findAll();
                return ResponseEntity.ok(ApiResponse.success(buildings));
            }
            
            // 使用模糊查询
            List<Building> buildings = buildingRepository.findByNameContaining(query.trim());
            return ResponseEntity.ok(ApiResponse.success(buildings));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("搜索楼房失败: " + e.getMessage()));
        }
    }
} 