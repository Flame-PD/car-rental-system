package com.example.carrental.repository;

import com.example.carrental.entity.RentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RentRecordRepository extends JpaRepository<RentRecord, Integer> {

    // 查询某辆车的未还记录
    List<RentRecord> findByCarIdAndStatus(Integer carId, String status);

    // 按姓名模糊查询
    List<RentRecord> findByCustomerNameContaining(String customerName);

    // 按电话查询
    List<RentRecord> findByCustomerPhone(String customerPhone);

    // 查询所有未还记录
    List<RentRecord> findByStatus(String status);

    // 查询某用户的租赁中记录
    List<RentRecord> findByUserIdAndStatus(Integer userId, String status);

    // 查询某用户的所有记录
    List<RentRecord> findByUserId(Integer userId);
}