package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import java.util.Date;
import java.util.List;

public class PatrolLogRequest {
    @JsonProperty("task_id")
    private Long taskId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    @JsonProperty("start_time")
    private Date startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    @JsonProperty("end_time")
    private Date endTime;
    
    private String route;
    
    @JsonProperty("has_abnormal")
    private Boolean hasAbnormal = false;
    
    @JsonProperty("abnormal_desc")
    private String abnormalDesc;
    
    @JsonProperty(value = "images", access = JsonProperty.Access.WRITE_ONLY)
    private List<MultipartFile> images;

    // Getters and Setters
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
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

    public List<MultipartFile> getImages() {
        return images;
    }

    public void setImages(List<MultipartFile> images) {
        this.images = images;
    }
} 