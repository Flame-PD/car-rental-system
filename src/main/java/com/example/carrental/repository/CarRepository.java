package com.example.carrental.repository;
import com.example.carrental.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CarRepository extends JpaRepository<Car, Integer> {
    List<Car> findByStatus(String status);
}
