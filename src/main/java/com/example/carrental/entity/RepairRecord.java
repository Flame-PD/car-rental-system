package com.example.carrental.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "repair_records")
public class RepairRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "car_id")
    private Integer carId;

    @Column(name = "car_plate")
    private String carPlate;

    @Column(name = "car_brand")
    private String carBrand;

    @Column(name = "car_model")
    private String carModel;

    @Column(name = "repair_start_time")
    private LocalDateTime repairStartTime;

    @Column(name = "repair_end_time")
    private LocalDateTime repairEndTime;

    @Column(name = "status")
    private String status;

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCarId() {
        return carId;
    }

    public void setCarId(Integer carId) {
        this.carId = carId;
    }

    public String getCarPlate() {
        return carPlate;
    }

    public void setCarPlate(String carPlate) {
        this.carPlate = carPlate;
    }

    public String getCarBrand() {
        return carBrand;
    }

    public void setCarBrand(String carBrand) {
        this.carBrand = carBrand;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public LocalDateTime getRepairStartTime() {
        return repairStartTime;
    }

    public void setRepairStartTime(LocalDateTime repairStartTime) {
        this.repairStartTime = repairStartTime;
    }

    public LocalDateTime getRepairEndTime() {
        return repairEndTime;
    }

    public void setRepairEndTime(LocalDateTime repairEndTime) {
        this.repairEndTime = repairEndTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Column(name = "cost")
    private Double cost;

    // Getter 和 Setter（自动生成）
    // 右键 → Generate → Getter and Setter → 全选
}