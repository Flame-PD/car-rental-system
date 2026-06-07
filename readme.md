# 车辆租赁管理系统（Car Rental System）

基于 Spring Boot + MySQL + Thymeleaf 的车辆租赁管理系统，实现车辆管理、租车还车、维修管理、利润分析等核心功能。

## 技术栈

- **后端框架**：Spring Boot 3.x
- **数据库**：MySQL 8.0
- **数据库访问**：Spring Data JPA
- **前端模板**：Thymeleaf
- **构建工具**：Maven
- **JDK 版本**：17 及以上

## 功能模块

### 客户端（面向租车用户）
- 车辆列表展示（分车型、品牌）
- 租车（弹窗填写姓名、电话、租车用途）
- 还车（自动计算租金，车辆自动进入维修）
- 按姓名/电话查询租车记录
- 用户注册 / 登录（租车必须登录）

### 管理端（面向管理员）
- 车辆管理：增删改查、状态修改（可租/在租/不可租）
- 租车记录管理：查看、筛选（按姓名/电话/车牌/时间/状态）、分页、还车
- 维修管理：还车自动生成维修单，维修完成后车辆恢复“可租”
- 利润分析：
  - 系统总利润
  - 各车辆利润统计
  - 月度收入/支出柱状图
  - 各品牌利润饼图
  - 利润明细表

## 自动业务规则

- 还车 → 自动结算租金（不足一天按一天算）→ 车辆变“不可租” → 自动生成维修记录
- 维修完成 → 车辆变“可租” → 自动记录维修支出
- 每一笔收入和支出 → 自动写入利润表，实时累加总利润

## 项目结构（简化）
car-rental-system/
├── src/main/java/com/example/carrental/
│ ├── controller/ # 控制器（处理请求）
│ ├── entity/ # 实体类（对应数据库表）
│ ├── repository/ # 数据访问接口
│ ├── config/ # 配置类（安全、命名策略）
│ └── CarrentalApplication.java # 启动类
├── src/main/resources/
│ ├── templates/ # Thymeleaf 模板
│ │ ├── admin/ # 管理端页面
│ │ ├── user/ # 登录/注册页面
│ │ └── *.html
│ └── application.properties # 主配置文件（需自行添加）
├── pom.xml
└── README.md


## 数据库设计（表结构）

> 详细建表语句见本文档下方「数据库初始化」章节。

主要表：
- `NewTable`：车辆表
- `RentRecord`：租车记录
- `repair_records`：维修记录
- `profit_records`：利润记录
- `users`：用户表

## 环境要求

- JDK 17 或以上
- MySQL 8.0
- Maven 3.8+（IDEA 通常自带）
- Git（可选，用于克隆）

## 数据库初始化（重点）

### 1. 创建数据库

在 MySQL 中执行：

```sql
CREATE DATABASE IF NOT EXISTS car_rental;
USE car_rental;
```

###  2.创建表及基础数据
直接复制以下 SQL 到 MySQL 执行：
```sql
-- 车辆表
CREATE TABLE IF NOT EXISTS NewTable (
    id INT PRIMARY KEY AUTO_INCREMENT,
    Plate VARCHAR(10) NOT NULL,
    Brand VARCHAR(20),
    Model VARCHAR(30),
    Price_per_day DECIMAL(10,2),
    Status ENUM('可租', '在租', '不可租') DEFAULT '可租'
);

-- 租车记录表
CREATE TABLE IF NOT EXISTS RentRecord (
    id INT PRIMARY KEY AUTO_INCREMENT,
    CarId INT NOT NULL,
    RentTime DATETIME,
    ReturnTime DATETIME,
    CustomerName VARCHAR(50),
    CustomerPhone VARCHAR(20),
    PriceNow DECIMAL(10,2),
    PriceTotal DECIMAL(10,2),
    Status ENUM('租赁中', '已归还') DEFAULT '租赁中',
    purpose VARCHAR(20) DEFAULT '其他',
    user_id INT,
    FOREIGN KEY (CarId) REFERENCES NewTable(id)
);

-- 维修记录表
CREATE TABLE IF NOT EXISTS repair_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    car_id INT NOT NULL,
    car_plate VARCHAR(20),
    car_brand VARCHAR(50),
    car_model VARCHAR(50),
    repair_start_time DATETIME NOT NULL,
    repair_end_time DATETIME,
    status VARCHAR(20) DEFAULT '维修中',
    cost DECIMAL(10,2) DEFAULT 50.00,
    FOREIGN KEY (car_id) REFERENCES NewTable(id)
);

-- 利润记录表
CREATE TABLE IF NOT EXISTS profit_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    car_id INT NOT NULL,
    car_plate VARCHAR(20),
    car_brand VARCHAR(50),
    car_model VARCHAR(50),
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    related_id INT,
    record_time DATETIME NOT NULL,
    running_balance DECIMAL(10,2) NOT NULL,
    remark VARCHAR(100),
    FOREIGN KEY (car_id) REFERENCES NewTable(id)
);

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    real_name VARCHAR(50),
    role VARCHAR(20) DEFAULT 'USER',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 给 RentRecord 增加外键（如果上面未生效）
ALTER TABLE RentRecord ADD FOREIGN KEY (user_id) REFERENCES users(id);
```

### 3. 插入初始数据（可选）

```sql
-- 车辆数据
INSERT INTO NewTable (Plate, Brand, Model, Price_per_day, Status) VALUES
('赣A00000', '宝马', 'X1', 200.00, '可租'),
('京A12345', '丰田', '卡罗拉', 180.00, '可租'),
('沪B67890', '本田', '雅阁', 250.00, '可租'),
('粤C11111', '宝马', 'X3', 450.00, '可租'),
('浙D22222', '奔驰', 'C260L', 380.00, '可租'),
('苏E33333', '大众', '帕萨特', 220.00, '可租'),
('川F44444', '特斯拉', 'Model 3', 350.00, '可租'),
('鲁G55555', '比亚迪', '汉EV', 280.00, '可租'),
('湘H66666', '奥迪', 'A4L', 400.00, '可租'),
('闽J77777', '蔚来', 'ET5', 320.00, '可租');

-- 用户数据（密码明文，仅用于测试）
INSERT INTO users (username, password, phone, real_name, role) VALUES
('admin', 'admin123', '13900000000', '管理员', 'ADMIN'),
('zhangsan', '123456', '13800138001', '张三', 'USER'),
('lisi', '123456', '13800138002', '李四', 'USER');
```

##项目配置（关键）
在 src/main/resources/application.properties 中配置数据库连接（模板）：
```java
spring.datasource.url=jdbc:mysql://localhost:3306/car_rental?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=你的密码
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
```
>如果你的 MySQL 在 WSL 中，请将 localhost 改为 WSL 的 IP 地址（例如 172.xx.xx.xx）。

##运行项目

###方式一：IDEA 直接运行

打开项目，等待 Maven 下载依赖

找到 CarrentalApplication.java

右键 → Run

###方式二：命令行运行

```bash
mvn spring-boot:run
```

##访问地址

页面	地址	说明
客户端首页	http://localhost:8080/cars	车辆列表、租车/还车
用户登录	http://localhost:8080/user/login	普通用户登录
用户注册	http://localhost:8080/user/register	注册新用户
管理后台	http://localhost:8080/admin	管理员后台
车辆管理	http://localhost:8080/admin/cars	车辆增删改查
租车记录	http://localhost:8080/admin/rent-records	租车记录管理
维修管理	http://localhost:8080/admin/repairs	维修管理
利润分析	http://localhost:8080/admin/profit	利润报表/图表
>管理员账号：admin / admin123

##许可证

本项目使用 MIT 许可证，详见 LICENSE 文件。

##作者

秦沛儒

课程设计 / 车辆租赁管理系统

2026年6月