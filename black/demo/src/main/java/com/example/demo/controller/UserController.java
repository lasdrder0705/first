package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.UserProfileResponse;
import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.util.JwtUtil;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            @RequestHeader("Authorization") String token) {
        try {
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            UserProfileResponse profile = new UserProfileResponse();
            profile.setId(user.getId());
            profile.setUsername(user.getUsername());
            profile.setNickname(user.getNickname());
            profile.setEmail(user.getEmail());
            profile.setPhone(user.getPhone());
            profile.setAvatar(user.getAvatar());
            profile.setRole(user.getRole());
            profile.setBio(user.getBio());
            profile.setCreatedAt(user.getCreatedAt());

            return ResponseEntity.ok(ApiResponse.success(profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取个人信息失败: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdateProfileRequest request) {
        try {
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // 只更新非空字段，保留原有数据
            if (StringUtils.hasText(request.getNickname())) {
                user.setNickname(request.getNickname());
            }
            if (StringUtils.hasText(request.getEmail())) {
                user.setEmail(request.getEmail());
            }
            
            // 修改这里的 bio 更新逻辑
            user.setBio(request.getBio());  // 允许设置为空字符串

            // 确保不会清空头像
            if (user.getAvatar() == null) {
                user.setAvatar("/default-avatar.png");
            }

            // 保存到数据库
            user = userRepository.save(user);

            // 构建响应
            UserProfileResponse profile = new UserProfileResponse();
            profile.setId(user.getId());
            profile.setUsername(user.getUsername());
            profile.setNickname(user.getNickname());
            profile.setEmail(user.getEmail());
            profile.setBio(user.getBio());
            profile.setAvatar(user.getAvatar());
            profile.setRole(user.getRole());
            profile.setCreatedAt(user.getCreatedAt());

            return ResponseEntity.ok(ApiResponse.success(profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("更新个人信息失败: " + e.getMessage()));
        }
    }

    @PostMapping("/profile/avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("请选择文件"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("只能上传图片文件"));
            }

            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // 确保上传目录存在
            File uploadPath = new File(uploadDir);
            if (!uploadPath.exists()) {
                uploadPath.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("文件名不能为空"));
            }
            
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = Paths.get(uploadDir, filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 删除旧头像文件
            String oldAvatar = user.getAvatar();
            if (oldAvatar != null && !oldAvatar.equals("/default-avatar.png")) {
                try {
                    String oldFilename = oldAvatar.substring(oldAvatar.lastIndexOf("/") + 1);
                    Path oldFilePath = Paths.get(uploadDir, oldFilename);
                    Files.deleteIfExists(oldFilePath);
                } catch (Exception e) {
                    // 记录错误但继续执行
                    System.err.println("删除旧头像失败: " + e.getMessage());
                }
            }

            // 更新用户头像URL
            String avatarUrl = "/api/uploads/" + filename;
            user.setAvatar(avatarUrl);
            userService.updateUser(user);

            return ResponseEntity.ok(ApiResponse.success(avatarUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("上传头像失败: " + e.getMessage()));
        }
    }

    // 内部类用于头像上传响应
    private static class AvatarResponse {
        private String avatar;

        public AvatarResponse(String avatar) {
            this.avatar = avatar;
        }

        public String getAvatar() {
            return avatar;
        }
    }

    // 内部类用于错误响应
    private static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
} 