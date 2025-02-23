package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public class MaintenanceHandleRequest {
    @JsonProperty("maintenance_id")
    private Long maintenanceId;
    
    private String result;
    private String status;
    
    @JsonProperty("handled_at")
    private Date handledAt;

    // Getters and Setters
    public Long getMaintenanceId() {
        return maintenanceId;
    }

    public void setMaintenanceId(Long maintenanceId) {
        this.maintenanceId = maintenanceId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getHandledAt() {
        return handledAt;
    }

    public void setHandledAt(Date handledAt) {
        this.handledAt = handledAt;
    }
} 