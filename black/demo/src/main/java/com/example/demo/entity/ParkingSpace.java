package com.example.demo.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "parking_spaces", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"building", "space_number"})
})
public class ParkingSpace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "building")
    private Long building;  // 楼房ID

    @Column(name = "space_number")
    private String spaceNumber;  // 车位编号

    @Column(nullable = false)
    private String location;     // 位置

    @Column(nullable = false)
    private String type;         // 类型（地上/地下）

    @Column(nullable = false)
    private String status = "空闲";  // 默认状态

    private Double price;        // 价格
    private String size;         // 尺寸
    private String description;  // 描述
    private Double area;

    @Column(name = "allowed_vehicle_type")
    private String allowedVehicleType;  // 可停车型

    @Column(name = "charging_pile")
    private String chargingPile = "无";  // 默认无充电桩

    @Column(name = "owner_name")
    private String ownerName;    // 业主姓名

    @Column(name = "contact_number")
    private String contactNumber; // 联系电话

    @Column(name = "rental_start")
    @Temporal(TemporalType.DATE)
    private Date rentalStart;    // 租期开始

    @Column(name = "rental_end")
    @Temporal(TemporalType.DATE)
    private Date rentalEnd;      // 租期结束

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpaceNumber() {
        return spaceNumber;
    }

    public void setSpaceNumber(String spaceNumber) {
        this.spaceNumber = spaceNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBuilding() {
        return building != null ? building.toString() : null;
    }

    public void setBuilding(Long building) {
        this.building = building;
    }

    public String getAllowedVehicleType() {
        return allowedVehicleType;
    }

    public void setAllowedVehicleType(String allowedVehicleType) {
        this.allowedVehicleType = allowedVehicleType;
    }

    public String getChargingPile() {
        return chargingPile;
    }

    public void setChargingPile(String chargingPile) {
        this.chargingPile = chargingPile;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Date getRentalStart() {
        return rentalStart;
    }

    public void setRentalStart(Date rentalStart) {
        this.rentalStart = rentalStart;
    }

    public Date getRentalEnd() {
        return rentalEnd;
    }

    public void setRentalEnd(Date rentalEnd) {
        this.rentalEnd = rentalEnd;
    }
} 