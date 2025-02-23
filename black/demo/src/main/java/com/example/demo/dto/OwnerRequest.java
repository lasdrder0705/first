package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OwnerRequest {
    private Long id;
    private String name;
    
    @JsonProperty("contact_number")
    private String contactNumber;
    
    @JsonProperty("building_id")
    private Long buildingId;
    
    @JsonProperty("door_number")
    private String doorNumber;
    
    @JsonProperty("parking_space")
    private String parkingSpace;
    
    private List<Long> vehicles;

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

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public String getDoorNumber() {
        return doorNumber;
    }

    public void setDoorNumber(String doorNumber) {
        this.doorNumber = doorNumber;
    }

    public List<Long> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Long> vehicles) {
        this.vehicles = vehicles;
    }

    public String getParkingSpace() {
        return parkingSpace;
    }

    public void setParkingSpace(String parkingSpace) {
        this.parkingSpace = parkingSpace;
    }
} 