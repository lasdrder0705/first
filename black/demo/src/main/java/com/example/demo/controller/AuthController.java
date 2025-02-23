package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 参数验证
            if (loginRequest == null || 
                loginRequest.getUsername() == null || 
                loginRequest.getPassword() == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("用户名和密码不能为空"));
            }

            // 管理员登录
            if ("admin".equals(loginRequest.getUsername())) {
                User adminUser = userRepository.findByUsername("admin")
                        .orElse(null);
                if (adminUser == null) {
                    adminUser = new User();
                    adminUser.setUsername("admin");
                    adminUser.setPassword("admin");
                    adminUser.setRole("admin");
                    adminUser.setNickname("管理员");
                    adminUser.setAvatar("/default-avatar.png");
                    adminUser.setEmail("admin@example.com");
                    adminUser.setCreatedAt(new Date());
                    userRepository.save(adminUser);
                }
                if ("admin".equals(loginRequest.getPassword())) {
                    // 管理员使用不带权限的 token 生成方法
                    String token = jwtUtil.generateToken(adminUser.getUsername(), adminUser.getRole());
                    return ResponseEntity.ok(ApiResponse.success(new LoginResponse(token)));
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("密码错误"));
                }
            }

            // 普通用户或员工登录
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElse(null);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户不存在"));
            }

            // 验证密码
            if (!user.getPassword().equals(loginRequest.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("密码错误"));
            }

            // 生成包含权限信息的 token
            List<String> permissions = new ArrayList<>();
            if (user.getPermissions() != null && !user.getPermissions().isEmpty()) {
                try {
                    permissions = objectMapper.readValue(user.getPermissions(), 
                        new TypeReference<List<String>>() {});
                } catch (Exception e) {
                    System.err.println("解析权限失败：" + e.getMessage());
                }
            }

            String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), permissions);
            return ResponseEntity.ok(ApiResponse.success(new LoginResponse(token)));

        } catch (Exception e) {
            e.printStackTrace(); // 添加这行来帮助调试
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("登录失败: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("用户名已存在"));
            }

            // 创建新用户
            User newUser = new User();
            newUser.setUsername(registerRequest.getUsername());
            newUser.setPassword(registerRequest.getPassword()); // 实际应用中应该加密
            newUser.setRole("user"); // 默认角色
            newUser.setAvatar("/default-avatar.png"); // 默认头像
            newUser.setCreatedAt(new Date());
            
            userRepository.save(newUser);

            // 注册成功后自动登录
            String token = jwtUtil.generateToken(registerRequest.getUsername(), "user", new ArrayList<>());
            return ResponseEntity.ok(ApiResponse.success(new LoginResponse(token)));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("注册失败: " + e.getMessage()));
        }
    }
} 