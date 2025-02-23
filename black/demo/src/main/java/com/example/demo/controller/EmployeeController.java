package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.EmployeeRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "http://localhost:5173")
public class EmployeeController {

    @Autowired
    private UserRepository userRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    // 获取所有员工（排除admin和普通注册的user）
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllEmployees() {
        try {
            List<User> employees = userRepository.findByRoleNotIn(Arrays.asList("admin", "user"));
            return ResponseEntity.ok(ApiResponse.success(employees));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取员工列表失败: " + e.getMessage()));
        }
    }

    // 新增员工接口
    @PostMapping
    public ResponseEntity<ApiResponse<User>> createEmployee(@RequestBody EmployeeRequest request) {
        try {
            // 打印接收到的请求数据
            System.out.println("Received employee request: " + objectMapper.writeValueAsString(request));
            
            // 验证必填字段
            if (request.getName() == null || request.getName().trim().isEmpty() ||
                request.getIdCard() == null || request.getIdCard().trim().isEmpty() ||
                request.getRole() == null || request.getRole().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("姓名、身份证号和职位不能为空"));
            }

            // 验证身份证号格式
            String idCardRegex = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)";
            if (!request.getIdCard().matches(idCardRegex)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("身份证号格式不正确"));
            }

            // 检查身份证号是否已存在
            if (userRepository.existsByUsername(request.getIdCard())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("该身份证号已被注册"));
            }

            User newEmployee = new User();
            // 使用身份证号作为登录账号
            newEmployee.setUsername(request.getIdCard());
            // 使用身份证后8位作为初始密码
            String idCard = request.getIdCard();
            String password = idCard.length() >= 8 ? 
                idCard.substring(idCard.length() - 8) : idCard;
            newEmployee.setPassword(password);
            
            newEmployee.setNickname(request.getName());
            newEmployee.setRole(request.getRole());
            
            // 保存权限信息
            if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
                // 确保权限列表被正确转换为 JSON 数组
                String permissionsJson = objectMapper.writeValueAsString(request.getPermissions());
                newEmployee.setPermissions(permissionsJson);
            } else {
                // 设置空数组的 JSON 字符串
                newEmployee.setPermissions("[]");
            }
            
            newEmployee.setAvatar("/default-avatar.png");
            newEmployee.setCreatedAt(new Date());
            
            User savedEmployee = userRepository.save(newEmployee);
            return ResponseEntity.ok(ApiResponse.success(savedEmployee, 
                String.format("员工创建成功！\n登录账号：%s\n初始密码：%s", 
                    request.getIdCard(), password)));
        } catch (Exception e) {
            // 打印详细错误信息
            System.err.println("Error creating employee: ");
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建员工失败: " + e.getMessage()));
        }
    }

    // 编辑员工信息接口
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateEmployee(@PathVariable Long id,
                                                            @RequestBody EmployeeRequest request) {
        try {
            Optional<User> optionalEmp = userRepository.findById(id);
            if (!optionalEmp.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            User existingEmployee = optionalEmp.get();
            
            // 如果身份证号改变，检查新的身份证号是否已被其他用户使用
            if (!existingEmployee.getUsername().equals(request.getIdCard())) {
                // 检查新的身份证号是否被其他用户使用（排除当前用户）
                boolean idCardExists = userRepository.findByUsername(request.getIdCard())
                    .map(user -> !user.getId().equals(id))
                    .orElse(false);
                    
                if (idCardExists) {
                    return ResponseEntity.badRequest()
                        .body(ApiResponse.error("该身份证号已被其他员工使用"));
                }
                
                // 更新用户名（身份证号）和密码
                existingEmployee.setUsername(request.getIdCard());
                String idCard = request.getIdCard();
                String password = idCard.length() >= 8 ? 
                    idCard.substring(idCard.length() - 8) : idCard;
                existingEmployee.setPassword(password);
            }
            
            // 更新其他信息
            existingEmployee.setNickname(request.getName());
            existingEmployee.setRole(request.getRole());
            
            // 更新权限
            if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
                String permissionsJson = objectMapper.writeValueAsString(request.getPermissions());
                existingEmployee.setPermissions(permissionsJson);
            } else {
                existingEmployee.setPermissions("[]");
            }
            
            User savedEmployee = userRepository.save(existingEmployee);
            return ResponseEntity.ok(ApiResponse.success(savedEmployee, "员工信息更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("更新员工信息失败: " + e.getMessage()));
        }
    }

    // 删除员工接口
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        try {
            Optional<User> optionalEmp = userRepository.findById(id);
            if (!optionalEmp.isPresent()) {
                return ResponseEntity.notFound()
                    .build();
            }
            
            // 执行删除操作
            userRepository.deleteById(id);
            
            return ResponseEntity.ok(ApiResponse.success(null, "删除成功1"));
        } catch (Exception e) {
            e.printStackTrace(); // 添加日志输出
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("删除员工失败: " + e.getMessage()));
        }
    }
} 