package com.example.demo.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "cleaning_logs")
public class CleaningLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "cleaner_id", nullable = false)
    private Long cleanerId;  // 保洁员ID

    @Column(name = "cleaner_name", nullable = false)
    private String cleanerName;  // 保洁员姓名

    @Column(name = "time_slot", nullable = false)
    private String timeSlot;  // 时间段

    @Column(nullable = false)
    private String area;  // 清洁区域

    @Column(name = "start_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;  // 开始时间

    @Column(name = "end_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;  // 结束时间

    @Column(nullable = false)
    private String content;  // 清洁内容

    @Column(name = "hygiene_condition", nullable = false)
    private String hygieneCondition;  // 卫生情况

    @Column(name = "condition_desc")
    private String conditionDesc;  // 情况说明

    @Column(name = "has_damage")
    private Boolean hasDamage = false;  // 是否有设施损坏

    @Column(name = "damage_desc")
    private String damageDesc;  // 损坏说明

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;  // 图片URL列表，JSON格式存储

    @Column(name = "status")
    private String status = "待处理";  // 状态：待处理、处理中、已完成

    @Column(name = "repair_result")
    private String repairResult;  // 处理结果

    @Column(name = "repair_time")
    private Date repairTime;  // 处理时间

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

    public Long getCleanerId() {
        return cleanerId;
    }

    public void setCleanerId(Long cleanerId) {
        this.cleanerId = cleanerId;
    }

    public String getCleanerName() {
        return cleanerName;
    }

    public void setCleanerName(String cleanerName) {
        this.cleanerName = cleanerName;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHygieneCondition() {
        return hygieneCondition;
    }

    public void setHygieneCondition(String hygieneCondition) {
        this.hygieneCondition = hygieneCondition;
    }

    public String getConditionDesc() {
        return conditionDesc;
    }

    public void setConditionDesc(String conditionDesc) {
        this.conditionDesc = conditionDesc;
    }

    public Boolean getHasDamage() {
        return hasDamage;
    }

    public void setHasDamage(Boolean hasDamage) {
        this.hasDamage = hasDamage;
    }

    public String getDamageDesc() {
        return damageDesc;
    }

    public void setDamageDesc(String damageDesc) {
        this.damageDesc = damageDesc;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRepairResult() {
        return repairResult;
    }

    public void setRepairResult(String repairResult) {
        this.repairResult = repairResult;
    }

    public Date getRepairTime() {
        return repairTime;
    }

    public void setRepairTime(Date repairTime) {
        this.repairTime = repairTime;
    }
} 