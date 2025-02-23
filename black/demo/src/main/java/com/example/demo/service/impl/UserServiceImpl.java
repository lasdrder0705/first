package com.example.demo.service.impl;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElse(null);
    }

    @Override
    public User updateUser(User user) {
        // 确保更新前后的数据一致性
        User existingUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // 更新字段
        existingUser.setNickname(user.getNickname());
        existingUser.setEmail(user.getEmail());
        existingUser.setBio(user.getBio());
        existingUser.setAvatar(user.getAvatar());

        // 保存并返回更新后的用户
        return userRepository.save(existingUser);
    }
} 