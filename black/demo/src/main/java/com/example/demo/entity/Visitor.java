package com.example.demo.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "visitors")
public class Visitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "id_card", nullable = false)
    private String idCard;

    @Column(nullable = false)
    private String phone;

    @Column(name = "visit_type", nullable = false)
    private String visitType;

    @Column(nullable = false)
    private String purpose;

    @Column(name = "visit_time", nullable = false)
    private Date visitTime;

    @Column(name = "expected_leave_time")
    private Date expectedLeaveTime;

    @Column(name = "actual_leave_time")
    private Date actualLeaveTime;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "relation_with_owner")
    private String relationWithOwner;

    private String remarks;

    private String status;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Date getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(Date visitTime) {
        this.visitTime = visitTime;
    }

    public Date getExpectedLeaveTime() {
        return expectedLeaveTime;
    }

    public void setExpectedLeaveTime(Date expectedLeaveTime) {
        this.expectedLeaveTime = expectedLeaveTime;
    }

    public Date getActualLeaveTime() {
        return actualLeaveTime;
    }

    public void setActualLeaveTime(Date actualLeaveTime) {
        this.actualLeaveTime = actualLeaveTime;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getRelationWithOwner() {
        return relationWithOwner;
    }

    public void setRelationWithOwner(String relationWithOwner) {
        this.relationWithOwner = relationWithOwner;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 