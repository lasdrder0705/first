package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "owners")
public class Owner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 业主姓名，必填
    @Column(nullable = false)
    private String name;

    // 联系电话
    @Column(name = "contact_number")
    private String contactNumber;

    // 所属楼房ID
    @Column(name = "building_id")
    private Long buildingId;

    // 门牌号，例如"101室"
    @Column(name = "door_number")
    private String doorNumber;

    // 车辆信息，存储为 JSON 格式的字符串
    @Column(columnDefinition = "TEXT")
    private String vehicles;

    // 车位归属
    @Column(name = "parking_space")
    private String parkingSpace;

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

    public String getVehicles() {
        return vehicles;
    }

    public void setVehicles(String vehicles) {
        this.vehicles = vehicles;
    }

    public String getParkingSpace() {
        return parkingSpace;
    }

    public void setParkingSpace(String parkingSpace) {
        this.parkingSpace = parkingSpace;
    }
} 