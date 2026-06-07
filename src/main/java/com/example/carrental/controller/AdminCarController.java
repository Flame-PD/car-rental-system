package com.example.carrental.controller;

import com.example.carrental.entity.Car;
import com.example.carrental.repository.CarRepository;
import com.example.carrental.entity.RentRecord;
import com.example.carrental.repository.RentRecordRepository;
import com.example.carrental.entity.RepairRecord;
import com.example.carrental.repository.RepairRecordRepository;
import com.example.carrental.entity.ProfitRecord;
import com.example.carrental.repository.ProfitRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Controller
@RequestMapping("/admin")
public class AdminCarController {

    @Autowired
    private CarRepository carRepository;
    @Autowired
    private RentRecordRepository rentRecordRepository;

    // 管理后台首页
    @GetMapping("")
    public String index() {
        return "admin/index";
    }
    // 管理端车辆列表
    @GetMapping("/cars")
    public String adminCars(Model model) {
        List<Car> cars = carRepository.findAll();
        model.addAttribute("cars", cars);
        return "admin/cars_list";
    }

    // 显示新增车辆表单
    @GetMapping("/cars/add")
    public String showAddForm(Model model) {
        model.addAttribute("car", new Car());
        return "admin/cars_form";
    }

    // 保存新增车辆
    @PostMapping("/cars")
    public String addCar(@ModelAttribute Car car) {
        car.setStatus("可租");
        carRepository.save(car);
        return "redirect:/admin/cars";
    }

    // 显示编辑车辆表单
    @GetMapping("/cars/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Car car = carRepository.findById(id).orElseThrow();
        model.addAttribute("car", car);
        return "admin/cars_form";
    }

    // 保存编辑后的车辆
    @PostMapping("/cars/edit/{id}")
    public String editCar(@PathVariable Integer id, @ModelAttribute Car car) {
        Car existingCar = carRepository.findById(id).orElseThrow();
        existingCar.setPlate(car.getPlate());
        existingCar.setBrand(car.getBrand());
        existingCar.setModel(car.getModel());
        existingCar.setPricePerDay(car.getPricePerDay());
        carRepository.save(existingCar);
        return "redirect:/admin/cars";
    }

    // 删除车辆
    @GetMapping("/cars/delete/{id}")
    public String deleteCar(@PathVariable Integer id) {
        carRepository.deleteById(id);
        return "redirect:/admin/cars";
    }
    // 更新车辆状态
    @PostMapping("/cars/update-status/{id}")
    @ResponseBody
    public String updateStatus(@PathVariable Integer id, @RequestParam String status) {
        Car car = carRepository.findById(id).orElseThrow();
        car.setStatus(status);
        carRepository.save(car);
        return "success";
    }


    // ========== 租车记录管理 ==========
    @GetMapping("/rent-records")
    public String rentRecordsList(Model model) {
        List<RentRecord> records = rentRecordRepository.findAll();
        model.addAttribute("records", records);
        return "admin/rent_records_list";
    }

    @GetMapping("/rent-records/add")
    public String showAddRentForm(Model model) {
        model.addAttribute("record", new RentRecord());
        model.addAttribute("cars", carRepository.findAll());
        return "admin/rent_records_form";
    }

    @PostMapping("/rent-records/add")
    public String addRentRecord(@ModelAttribute RentRecord record,
                                @RequestParam Integer carId) {
        Car car = carRepository.findById(carId).orElseThrow();
        record.setCarId(carId);
        record.setRentTime(LocalDateTime.now());
        record.setPriceNow(car.getPricePerDay());
        record.setStatus("租赁中");
        rentRecordRepository.save(record);

        car.setStatus("在租");
        carRepository.save(car);

        return "redirect:/admin/rent-records";
    }

    @GetMapping("/rent-records/return/{id}")
    public String returnCar(@PathVariable Integer id) {
        RentRecord record = rentRecordRepository.findById(id).orElseThrow();
        record.setReturnTime(LocalDateTime.now());

        long days = java.time.Duration.between(record.getRentTime(), record.getReturnTime()).toDays();
        if (days < 1) days = 1;
        double totalPrice = days * record.getPriceNow();
        record.setPriceTotal(totalPrice);
        record.setStatus("已归还");
        rentRecordRepository.save(record);

        Car car = carRepository.findById(record.getCarId()).orElseThrow();
        car.setStatus("可租");
        carRepository.save(car);

        return "redirect:/admin/rent-records";
    }

    @GetMapping("/rent-records/delete/{id}")
    public String deleteRentRecord(@PathVariable Integer id) {
        RentRecord record = rentRecordRepository.findById(id).orElseThrow();
        if ("租赁中".equals(record.getStatus())) {
            Car car = carRepository.findById(record.getCarId()).orElseThrow();
            car.setStatus("可租");
            carRepository.save(car);
        }
        rentRecordRepository.deleteById(id);
        return "redirect:/admin/rent-records";
    }

    @Autowired
    private RepairRecordRepository repairRecordRepository;

    // 维修管理列表
    @GetMapping("/repairs")
    public String repairsList(Model model) {
        List<RepairRecord> repairs = repairRecordRepository.findByStatus("维修中");
        model.addAttribute("repairs", repairs);
        return "admin/repairs_list";
    }

    // 维修完成

    @GetMapping("/repairs/complete/{id}")
    public String completeRepair(@PathVariable Integer id) {
        RepairRecord repair = repairRecordRepository.findById(id).orElseThrow();

        repair.setRepairEndTime(LocalDateTime.now());
        repair.setStatus("已完成");
        repairRecordRepository.save(repair);

        Car car = carRepository.findById(repair.getCarId()).orElseThrow();
        car.setStatus("可租");
        carRepository.save(car);

        // ===== 添加支出记录 =====
        Double currentBalance = profitRecordRepository.getTotalProfit();
        if (currentBalance == null) currentBalance = 0.0;

        ProfitRecord profitRecord = new ProfitRecord();
        profitRecord.setCarId(car.getId());
        profitRecord.setCarPlate(car.getPlate());
        profitRecord.setCarBrand(car.getBrand());
        profitRecord.setCarModel(car.getModel());
        profitRecord.setType("支出");
        profitRecord.setAmount(repair.getCost());
        profitRecord.setRelatedId(repair.getId());
        profitRecord.setRecordTime(LocalDateTime.now());
        profitRecord.setRunningBalance(currentBalance - repair.getCost());
        profitRecord.setRemark("维修支出 - " + repair.getCarPlate());
        profitRecordRepository.save(profitRecord);
        // ===== 添加完成 =====

        return "redirect:/admin/repairs";
    }

    /**/
    @Autowired
    private ProfitRecordRepository profitRecordRepository;

    // 利润分析首页
    // 利润分析首页
    @GetMapping("/profit")
    public String profitIndex(Model model) {
        // 系统总利润
        Double totalProfit = profitRecordRepository.getTotalProfit();
        model.addAttribute("totalProfit", totalProfit != null ? totalProfit : 0.0);

        // 所有利润记录
        List<ProfitRecord> records = profitRecordRepository.findAllByOrderByRecordTimeAsc();
        model.addAttribute("records", records);

        // 各车辆利润统计
        List<Car> cars = carRepository.findAll();
        model.addAttribute("cars", cars);

        // 计算每辆车的利润
        Map<Integer, Double> carProfitMap = new HashMap<>();
        for (Car car : cars) {
            Double profit = profitRecordRepository.getCarProfit(car.getId());
            carProfitMap.put(car.getId(), profit != null ? profit : 0.0);
        }
        model.addAttribute("carProfitMap", carProfitMap);
        // ========== 图表数据 ==========
        // 月度统计
        List<Object[]> monthlyStats = profitRecordRepository.getMonthlyStats();
        List<String> months = new ArrayList<>();
        List<Double> incomes = new ArrayList<>();
        List<Double> expenses = new ArrayList<>();
        for (Object[] stat : monthlyStats) {
            months.add((String) stat[0]);
            incomes.add(((Number) stat[1]).doubleValue());
            expenses.add(((Number) stat[2]).doubleValue());
        }
        model.addAttribute("months", months);
        model.addAttribute("incomes", incomes);
        model.addAttribute("expenses", expenses);

        // 品牌利润
        List<Object[]> brandStats = profitRecordRepository.getProfitByBrand();
        List<String> brands = new ArrayList<>();
        List<Double> brandProfits = new ArrayList<>();
        for (Object[] stat : brandStats) {
            brands.add((String) stat[0]);
            brandProfits.add(((Number) stat[1]).doubleValue());
        }
        model.addAttribute("brands", brands);
        model.addAttribute("brandProfits", brandProfits);
        return "admin/profit_index";
    }

    // 单辆车利润详情
    @GetMapping("/profit/car/{id}")
    public String carProfit(@PathVariable Integer id, Model model) {
        Car car = carRepository.findById(id).orElseThrow();
        model.addAttribute("car", car);

        Double carProfit = profitRecordRepository.getCarProfit(id);
        model.addAttribute("carProfit", carProfit);

        List<ProfitRecord> records = profitRecordRepository.findByCarIdOrderByRecordTimeAsc(id);
        model.addAttribute("records", records);

        return "admin/profit_car";
    }

}