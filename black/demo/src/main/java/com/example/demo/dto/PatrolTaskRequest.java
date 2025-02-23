package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PatrolTaskRequest {
    @JsonProperty("time_slot")
    private String timeSlot;
    
    private String area;
    private String focus;
    
    @JsonProperty("assigned_to")
    private Long assignedTo;

    // Getters and Setters
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

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }

    public Long getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Long assignedTo) {
        this.assignedTo = assignedTo;
    }
} 