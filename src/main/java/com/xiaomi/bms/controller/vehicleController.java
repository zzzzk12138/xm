package com.xiaomi.bms.controller;

import com.xiaomi.bms.entity.Vehicle;
import com.xiaomi.bms.service.vehicleService;
import com.xiaomi.bms.utils.VidGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/vehicle")
public class vehicleController {

    @Autowired
    private vehicleService vehicleService;

    /**
     * 创建新车辆
     * @param vehicle 车辆信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<?> createVehicle(@RequestBody Vehicle vehicle) {
        log.info("接收到创建车辆请求: {}", vehicle);
        // 初始先生成一个随机 vid
        String generateVid = VidGenerator.generateVid();
        vehicle.setVid(generateVid);
        try {
            Vehicle createdVehicle = vehicleService.createVehicle(vehicle);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "车辆创建成功");
            response.put("data", createdVehicle);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("创建车辆参数验证失败: {}, 车辆信息: {}", e.getMessage(), vehicle);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("创建车辆时发生错误: {}, 车辆信息: {}", e.getMessage(), vehicle, e);
            return ResponseEntity.badRequest().body("创建车辆时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 根据VID查询车辆
     * @param vid 车辆VID
     * @return 车辆信息
     */
    @GetMapping("/{vid}")
    public ResponseEntity<?> getVehicleByVid(@PathVariable String vid) {
        log.info("开始查询车辆, VID: {}", vid);
        try {
            Vehicle vehicle = vehicleService.getVehicleByVid(vid);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "车辆查询成功");
            response.put("data", vehicle);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("查询车辆失败: {}, VID: {}", e.getMessage(), vid);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("查询车辆时发生错误: {}, VID: {}", e.getMessage(), vid, e);
            return ResponseEntity.badRequest().body("查询车辆时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 根据车架号查询车辆
     * @param carId 车架号
     * @return 车辆信息
     */
    @GetMapping("/car/{carId}")
    public ResponseEntity<?> getVehicleByCarId(@PathVariable Integer carId) {
        log.info("开始查询车辆, 车架号: {}", carId);
        try {
            Vehicle vehicle = vehicleService.getVehicleByCarId(carId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "车辆查询成功");
            response.put("data", vehicle);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("查询车辆失败: {}, 车架号: {}", e.getMessage(), carId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("查询车辆时发生错误: {}, 车架号: {}", e.getMessage(), carId, e);
            return ResponseEntity.badRequest().body("查询车辆时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 查询所有未删除的车辆
     * @return 车辆列表
     */
    @GetMapping
    public ResponseEntity<?> getAllVehicles() {
        log.info("开始查询所有车辆");
        try {
            List<Vehicle> vehicles = vehicleService.getAllVehicles();
            log.info("查询到 {} 辆车", vehicles.size());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "查询所有车辆成功");
            response.put("data", vehicles);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询所有车辆时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("查询车辆时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 更新车辆信息
     * @param vid 车辆VID
     * @param vehicle 更新的车辆信息
     * @return 更新结果
     */
    @PutMapping("/{vid}")
    public ResponseEntity<?> updateVehicle(@PathVariable String vid, @RequestBody Vehicle vehicle) {
        log.info("开始更新车辆信息, VID: {}, 车辆信息: {}", vid, vehicle);
        try {
            vehicle.setVid(vid);
            Vehicle updatedVehicle = vehicleService.updateVehicle(vehicle);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "车辆信息更新成功");
            response.put("data", updatedVehicle);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("更新车辆信息失败: {}, VID: {}", e.getMessage(), vid);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("更新车辆信息时发生错误: {}, VID: {}, 车辆信息: {}", e.getMessage(), vid, vehicle, e);
            return ResponseEntity.badRequest().body("更新车辆信息时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 删除车辆（软删除）
     * @param vid 车辆VID
     * @return 删除结果
     */
    @DeleteMapping("/{vid}")
    public ResponseEntity<?> deleteVehicle(@PathVariable String vid) {
        log.info("开始删除车辆, VID: {}", vid);
        try {
            vehicleService.deleteVehicle(vid);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "车辆删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("删除车辆失败: {}, VID: {}", e.getMessage(), vid);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("删除车辆时发生错误: {}, VID: {}", e.getMessage(), vid, e);
            return ResponseEntity.badRequest().body("删除车辆时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 检查车辆是否存在且未删除
     * @param vid 车辆VID
     * @return 是否存在且未删除
     */
    @GetMapping("/{vid}/exists")
    public ResponseEntity<?> checkVehicleExists(@PathVariable String vid) {
        log.info("检查车辆是否存在, VID: {}", vid);
        try {
            boolean exists = vehicleService.existsActiveVehicle(vid);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "检查车辆存在状态成功");
            response.put("data", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("检查车辆是否存在时发生错误: {}, VID: {}", e.getMessage(), vid, e);
            return ResponseEntity.badRequest().body("检查车辆是否存在时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 检查指定车架号的车辆是否存在且未删除
     * @param carId 车架号
     * @return 是否存在且未删除
     */
    @GetMapping("/car/{carId}/exists")
    public ResponseEntity<?> checkVehicleExistsByCarId(@PathVariable Integer carId) {
        log.info("检查车辆是否存在, 车架号: {}", carId);
        try {
            boolean exists = vehicleService.existsActiveVehicleByCarId(carId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "检查车辆存在状态成功");
            response.put("data", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("检查车辆是否存在时发生错误: {}, 车架号: {}", e.getMessage(), carId, e);
            return ResponseEntity.badRequest().body("检查车辆是否存在时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 根据车架号更新车辆信息
     * @param carId 车架号
     * @param vehicle 更新的车辆信息
     * @return 更新结果
     */
    @PutMapping("/car/{carId}")
    public ResponseEntity<?> updateVehicleByCarId(@PathVariable Integer carId, @RequestBody Vehicle vehicle) {
        log.info("开始更新车辆信息, 车架号: {}, 车辆信息: {}", carId, vehicle);
        try {
            Vehicle updatedVehicle = vehicleService.updateVehicleByCarId(carId, vehicle);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "车辆信息更新成功");
            response.put("data", updatedVehicle);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("更新车辆信息失败: {}, 车架号: {}", e.getMessage(), carId);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("更新车辆信息时发生错误: {}, 车架号: {}, 车辆信息: {}", e.getMessage(), carId, vehicle, e);
            return ResponseEntity.badRequest().body("更新车辆信息时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 根据车架号删除车辆（软删除）
     * @param carId 车架号
     * @return 删除结果
     */
    @DeleteMapping("/car/{carId}")
    public ResponseEntity<?> deleteVehicleByCarId(@PathVariable Integer carId) {
        log.info("开始删除车辆, 车架号: {}", carId);
        try {
            vehicleService.deleteVehicleByCarId(carId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "车辆删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("删除车辆失败: {}, 车架号: {}", e.getMessage(), carId);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("删除车辆时发生错误: {}, 车架号: {}", e.getMessage(), carId, e);
            return ResponseEntity.badRequest().body("删除车辆时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 根据车架号获取电池类型名称
     * @param carId 车架号
     * @return 电池类型名称
     */
    @GetMapping("/car/{carId}/battery")
    public ResponseEntity<?> getVehicleBatteryType(@PathVariable Integer carId) {
        log.info("开始查询车辆电池类型, 车架号: {}", carId);
        try {
            String batteryTypeName = vehicleService.getBatteryTypeName(carId);
            return ResponseEntity.ok(batteryTypeName);
        } catch (Exception e) {
            log.error("查询车辆电池类型时发生错误: {}, 车架号: {}", e.getMessage(), carId, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
