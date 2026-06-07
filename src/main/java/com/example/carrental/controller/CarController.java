package com.example.carrental.controller;

import com.example.carrental.entity.Car;
import com.example.carrental.repository.CarRepository;
import com.example.carrental.entity.RentRecord;
import com.example.carrental.repository.RentRecordRepository;
import com.example.carrental.entity.RepairRecord;
import com.example.carrental.repository.RepairRecordRepository;
import com.example.carrental.entity.ProfitRecord;
import com.example.carrental.repository.ProfitRecordRepository;
import com.example.carrental.entity.User;
import com.example.carrental.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/cars")
public class CarController {

    @Autowired
    private CarRepository carRepository;
    @Autowired
    private RentRecordRepository rentRecordRepository;
    @Autowired
    private RepairRecordRepository repairRecordRepository;
    @Autowired
    private ProfitRecordRepository profitRecordRepository;

    // 查看所有车辆（返回网页）
    @GetMapping
    public String getAllCars(Model model) {
        List<Car> cars = carRepository.findAll();
        model.addAttribute("cars", cars);
        return "cars";
    }

//    // 租车
//    @GetMapping("/rent/{id}")
//    public String rentCar(@PathVariable Integer id) {
//        Car car = carRepository.findById(id).orElseThrow();
//        car.setStatus("在租");
//        carRepository.save(car);
//        return "redirect:/cars";
//    }
//
//    // 还车
//    @GetMapping("/return/{id}")
//    public String returnCar(@PathVariable Integer id) {
//        Car car = carRepository.findById(id).orElseThrow();
//        car.setStatus("可租");
//        carRepository.save(car);
//        return "redirect:/cars";
//    }
// 租车（带姓名电话）
// 租车（带姓名电话）- 用 POST
@PostMapping("/rent")
public String rentCar(@RequestParam Integer carId,
                      @RequestParam(required = false) String customerName,
                      @RequestParam(required = false) String customerPhone,
                      @RequestParam(required = false) String purpose,
                      HttpSession session,
                      HttpServletRequest request) {

    // 获取当前登录用户
    User currentUser = (User) session.getAttribute("currentUser");

    // 未登录不能租车
    if (currentUser == null) {
        return "redirect:/user/login";
    }

    Car car = carRepository.findById(carId).orElseThrow();

    if (!"可租".equals(car.getStatus())) {
        return "redirect:/cars?error=车辆不可租";
    }

    RentRecord record = new RentRecord();
    record.setCarId(car.getId());
    record.setRentTime(LocalDateTime.now());
    record.setCustomerName(currentUser.getRealName());
    record.setCustomerPhone(currentUser.getPhone());
    record.setPriceNow(car.getPricePerDay());
    record.setStatus("租赁中");
    record.setUserId(currentUser.getId());  // 绑定当前用户

    rentRecordRepository.save(record);

    car.setStatus("在租");
    carRepository.save(car);

    return "redirect:/cars";
}

    @GetMapping("/return/{id}")
    public String returnCar(@PathVariable Integer id, HttpSession session, HttpServletRequest request) {

        // 获取当前登录用户
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        Car car = carRepository.findById(id).orElseThrow();

        // 找到这辆车"租赁中"的租车记录
        List<RentRecord> records = rentRecordRepository.findByCarIdAndStatus(car.getId(), "租赁中");

        if (!records.isEmpty()) {
            RentRecord record = records.get(0);

            // 验证是否是当前用户租的车
            if (!record.getUserId().equals(currentUser.getId())) {
                // 改用 session 存错误信息，不用 URL 参数
                session.setAttribute("errorMsg", "这不是您租的车，不能还车");
                return "redirect:/cars";
            }

            record.setReturnTime(LocalDateTime.now());

            long days = java.time.Duration.between(record.getRentTime(), record.getReturnTime()).toDays();
            if (days < 1) days = 1;
            double totalPrice = days * record.getPriceNow();
            record.setPriceTotal(totalPrice);
            record.setStatus("已归还");
            rentRecordRepository.save(record);

            // 添加收入记录
            Double currentBalance = profitRecordRepository.getTotalProfit();
            if (currentBalance == null) currentBalance = 0.0;

            ProfitRecord profitRecord = new ProfitRecord();
            profitRecord.setCarId(car.getId());
            profitRecord.setCarPlate(car.getPlate());
            profitRecord.setCarBrand(car.getBrand());
            profitRecord.setCarModel(car.getModel());
            profitRecord.setType("收入");
            profitRecord.setAmount(totalPrice);
            profitRecord.setRelatedId(record.getId());
            profitRecord.setRecordTime(LocalDateTime.now());
            profitRecord.setRunningBalance(currentBalance + totalPrice);
            profitRecord.setRemark("租车收入 - " + record.getCustomerName());
            profitRecordRepository.save(profitRecord);
        }

        // 车辆状态改为"不可租"，进入维修
        car.setStatus("不可租");
        carRepository.save(car);

        // 创建维修记录
        RepairRecord repair = new RepairRecord();
        repair.setCarId(car.getId());
        repair.setCarPlate(car.getPlate());
        repair.setCarBrand(car.getBrand());
        repair.setCarModel(car.getModel());
        repair.setRepairStartTime(LocalDateTime.now());
        repair.setStatus("维修中");
        repair.setCost(50.00);
        repairRecordRepository.save(repair);

        return "redirect:/cars";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String type,
                         Model model) {
        if (keyword != null && !keyword.isEmpty()) {
            List<RentRecord> records;
            if ("phone".equals(type)) {
                records = rentRecordRepository.findByCustomerPhone(keyword);
            } else {
                records = rentRecordRepository.findByCustomerNameContaining(keyword);
            }
            model.addAttribute("records", records);
        }
        return "search";
    }
    @PostMapping("/clear-error")
    @ResponseBody
    public String clearError(HttpSession session) {
        session.removeAttribute("errorMsg");
        return "ok";
    }
}