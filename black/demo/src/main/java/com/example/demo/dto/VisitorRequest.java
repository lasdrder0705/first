package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public class VisitorRequest {
    private String name;
    
    @JsonProperty("id_card")
    private String idCard;
    
    private String phone;
    
    @JsonProperty("visit_type")
    private String visitType;
    
    private String purpose;
    
    @JsonProperty("expected_leave_time")
    private Date expectedLeaveTime;
    
    @JsonProperty("owner_id")
    private Long ownerId;
    
    @JsonProperty("relation_with_owner")
    private String relationWithOwner;
    
    private String remarks;

    // Getters and Setters
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

    public Date getExpectedLeaveTime() {
        return expectedLeaveTime;
    }

    public void setExpectedLeaveTime(Date expectedLeaveTime) {
        this.expectedLeaveTime = expectedLeaveTime;
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
} 