package com.example.demo.repository;

import com.example.demo.entity.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    Optional<Visitor> findByIdCard(String idCard);
    Optional<Visitor> findByPhone(String phone);
    
    @Query("SELECT v FROM Visitor v WHERE DATE(v.visitTime) = DATE(:date)")
    List<Visitor> findAllByVisitDate(Date date);
    
    List<Visitor> findByIdCardContainingOrPhoneContaining(String idCard, String phone);
} 