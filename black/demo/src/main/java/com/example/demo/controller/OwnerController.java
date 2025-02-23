package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.OwnerRequest;
import com.example.demo.entity.Owner;
import com.example.demo.entity.ParkingSpace;
import com.example.demo.repository.OwnerRepository;
import com.example.demo.repository.ParkingSpaceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/owners")
@CrossOrigin(origins = "http://localhost:5173")
public class OwnerController {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private ParkingSpaceRepository parkingSpaceRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    // 获取所有业主
    @GetMapping
    public ResponseEntity<ApiResponse<List<Owner>>> getAllOwners() {
        try {
            List<Owner> owners = ownerRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success(owners));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取业主列表失败: " + e.getMessage()));
        }
    }

    // 创建新业主
    @PostMapping
    public ResponseEntity<ApiResponse<Owner>> createOwner(@RequestBody OwnerRequest request) {
        try {
            System.out.println("Received owner request raw: " + request);
            System.out.println("Received owner request JSON: " + objectMapper.writeValueAsString(request));

            // 验证必填字段
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("业主姓名不能为空"));
            }

            Owner owner = new Owner();
            owner.setName(request.getName().trim());
            owner.setContactNumber(request.getContactNumber());
            owner.setBuildingId(request.getBuildingId());
            owner.setDoorNumber(request.getDoorNumber());
            owner.setParkingSpace(request.getParkingSpace());

            // 处理车辆信息
            try {
                List<Long> vehicleIds = request.getVehicles();
                String vehiclesJson = (vehicleIds != null && !vehicleIds.isEmpty())
                    ? objectMapper.writeValueAsString(vehicleIds)
                    : "[]";
                owner.setVehicles(vehiclesJson);
                System.out.println("Vehicles JSON: " + vehiclesJson);
            } catch (Exception e) {
                owner.setVehicles("[]");
                System.err.println("Error serializing vehicles: " + e.getMessage());
            }

            System.out.println("Saving owner entity: " + objectMapper.writeValueAsString(owner));
            Owner savedOwner = ownerRepository.save(owner);
            System.out.println("Saved owner result: " + objectMapper.writeValueAsString(savedOwner));
            
            return ResponseEntity.ok(ApiResponse.success(savedOwner));
        } catch (Exception e) {
            System.err.println("Error creating owner: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建业主失败: " + e.getMessage()));
        }
    }

    // 更新业主信息
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Owner>> updateOwner(@PathVariable Long id, 
                                                          @RequestBody OwnerRequest request) {
        try {
            Owner existingOwner = ownerRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("业主不存在"));
            // 更新信息
            existingOwner.setName(request.getName());
            existingOwner.setContactNumber(request.getContactNumber());
            existingOwner.setBuildingId(request.getBuildingId());
            existingOwner.setDoorNumber(request.getDoorNumber());
            existingOwner.setParkingSpace(request.getParkingSpace());
            if (request.getVehicles() != null) {
                String vehiclesJson = objectMapper.writeValueAsString(request.getVehicles());
                existingOwner.setVehicles(vehiclesJson);
            } else {
                // 传入为 null 则置为空 JSON 格式
                existingOwner.setVehicles("[]");
            }
            Owner updatedOwner = ownerRepository.save(existingOwner);
            return ResponseEntity.ok(ApiResponse.success(updatedOwner));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新业主失败: " + e.getMessage()));
        }
    }

    // 删除业主
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOwner(@PathVariable Long id) {
        try {
            // 查找业主
            Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("业主不存在"));
            
            // 如果业主有关联的车位，先清除车位的业主信息
            if (owner.getParkingSpace() != null && !owner.getParkingSpace().isEmpty()) {
                ParkingSpace space = parkingSpaceRepository.findBySpaceNumber(owner.getParkingSpace())
                    .orElse(null);
                if (space != null) {
                    space.setStatus("空闲");
                    space.setOwnerName(null);
                    parkingSpaceRepository.save(space);
                }
            }

            // 删除业主
            ownerRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.<Void>success(null, "删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("删除业主失败: " + e.getMessage()));
        }
    }
} 