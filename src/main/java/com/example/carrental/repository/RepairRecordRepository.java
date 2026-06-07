package com.example.carrental.repository;

import com.example.carrental.entity.RepairRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RepairRecordRepository extends JpaRepository<RepairRecord, Integer> {
    List<RepairRecord> findByStatus(String status);
    List<RepairRecord> findByCarId(Integer carId);
}