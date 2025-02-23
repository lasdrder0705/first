package com.example.demo.entity;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "maintenance")
public class Maintenance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;  // 报修类型

    @Column(nullable = false)
    private String urgency;  // 紧急程度

    @Column(name = "building_id", nullable = false)
    private Long buildingId;  // 所属楼栋ID

    @Column(nullable = false)
    private String location;  // 具体位置

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;  // 故障描述

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;  // 图片URL列表，JSON格式存储

    @Column(name = "created_by")
    private Long createdBy;  // 报修人ID

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;  // 创建时间

    @Column(nullable = false)
    private String status = "待处理";  // 状态：待处理、处理中、已完成

    private String result;  // 处理结果

    @Column(name = "handled_by")
    private Long handledBy;  // 处理人ID

    @Column(name = "handled_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date handledAt;  // 处理时间

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(Long handledBy) {
        this.handledBy = handledBy;
    }

    public Date getHandledAt() {
        return handledAt;
    }

    public void setHandledAt(Date handledAt) {
        this.handledAt = handledAt;
    }
} 