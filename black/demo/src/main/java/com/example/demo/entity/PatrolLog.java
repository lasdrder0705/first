package com.example.demo.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "patrol_logs")
public class PatrolLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "patroller_id", nullable = false)
    private Long patrollerId;  // 巡逻人员ID

    @Column(name = "patroller_name", nullable = false)
    private String patrollerName;  // 巡逻人员姓名

    @Column(name = "time_slot", nullable = false)
    private String timeSlot;  // 时间段

    @Column(nullable = false)
    private String area;  // 巡逻区域

    @Column(name = "start_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;  // 开始时间

    @Column(name = "end_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;  // 结束时间

    @Column(nullable = false)
    private String route;  // 巡逻路线

    @Column(name = "has_abnormal", nullable = false)
    private Boolean hasAbnormal = false;  // 是否有异常

    @Column(name = "abnormal_desc", columnDefinition = "TEXT")
    private String abnormalDesc;  // 异常描述

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;  // 图片URL列表，JSON格式存储

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getPatrollerId() {
        return patrollerId;
    }

    public void setPatrollerId(Long patrollerId) {
        this.patrollerId = patrollerId;
    }

    public String getPatrollerName() {
        return patrollerName;
    }

    public void setPatrollerName(String patrollerName) {
        this.patrollerName = patrollerName;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public Boolean getHasAbnormal() {
        return hasAbnormal;
    }

    public void setHasAbnormal(Boolean hasAbnormal) {
        this.hasAbnormal = hasAbnormal;
    }

    public String getAbnormalDesc() {
        return abnormalDesc;
    }

    public void setAbnormalDesc(String abnormalDesc) {
        this.abnormalDesc = abnormalDesc;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }
} 