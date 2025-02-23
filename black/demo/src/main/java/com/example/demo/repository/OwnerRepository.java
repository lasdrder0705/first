package com.example.demo.repository;

import com.example.demo.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    // 如有需要可增加模糊查询等方法
} 