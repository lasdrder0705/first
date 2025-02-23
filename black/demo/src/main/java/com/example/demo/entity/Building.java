package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "buildings")
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String image;
    private Integer floors;
    private Integer occupancy;
    private Integer total_units;
    private String status;

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getFloors() {
        return floors;
    }

    public void setFloors(Integer floors) {
        this.floors = floors;
    }

    public Integer getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(Integer occupancy) {
        this.occupancy = occupancy;
    }

    public Integer getTotal_units() {
        return total_units;
    }

    public void setTotal_units(Integer total_units) {
        this.total_units = total_units;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // 计算入住率
    public String getOccupancy_rate() {
        if (total_units == null || total_units == 0) {
            return "0%";
        }
        double rate = (occupancy * 100.0) / total_units;
        return String.format("%.1f%%", rate);
    }
} 