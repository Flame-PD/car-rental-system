package com.example.carrental.repository;

import com.example.carrental.entity.ProfitRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProfitRecordRepository extends JpaRepository<ProfitRecord, Integer> {
    // 查询某辆车的所有利润记录
    List<ProfitRecord> findByCarIdOrderByRecordTimeAsc(Integer carId);

    // 查询所有记录按时间排序
    List<ProfitRecord> findAllByOrderByRecordTimeAsc();

    // 查询某辆车总利润
    @Query("SELECT COALESCE(SUM(CASE WHEN p.type = '收入' THEN p.amount ELSE -p.amount END), 0) FROM ProfitRecord p WHERE p.carId = ?1")
    Double getCarProfit(Integer carId);

    // 查询系统总利润
    @Query("SELECT COALESCE(SUM(CASE WHEN p.type = '收入' THEN p.amount ELSE -p.amount END), 0) FROM ProfitRecord p")
    Double getTotalProfit();

    // 按月统计收入和支出
    @Query("SELECT FUNCTION('DATE_FORMAT', p.recordTime, '%Y-%m') as month, " +
            "SUM(CASE WHEN p.type = '收入' THEN p.amount ELSE 0 END) as income, " +
            "SUM(CASE WHEN p.type = '支出' THEN p.amount ELSE 0 END) as expense " +
            "FROM ProfitRecord p GROUP BY FUNCTION('DATE_FORMAT', p.recordTime, '%Y-%m') ORDER BY month")
    List<Object[]> getMonthlyStats();

    // 统计各车型利润（按品牌分组）
    @Query("SELECT p.carBrand, SUM(CASE WHEN p.type = '收入' THEN p.amount ELSE -p.amount END) as profit " +
            "FROM ProfitRecord p GROUP BY p.carBrand ORDER BY profit DESC")
    List<Object[]> getProfitByBrand();
}