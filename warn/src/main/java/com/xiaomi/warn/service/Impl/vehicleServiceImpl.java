package com.xiaomi.warn.service.Impl;

import com.xiaomi.warn.entity.Vehicle;
import com.xiaomi.warn.entity.BatteryType;
import com.xiaomi.warn.mapper.batMapper;
import com.xiaomi.warn.mapper.vehicleMapper;
import com.xiaomi.warn.service.vehicleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class vehicleServiceImpl implements vehicleService {
    
    @Autowired
    private vehicleMapper vehicleMapper;

    @Autowired
    private batMapper batMapper;

    @Override
    @Transactional
    public Vehicle createVehicle(Vehicle vehicle) {
        // 参数验证
        if (vehicle == null) {
            throw new IllegalArgumentException("车辆信息不能为空");
        }
        if (vehicle.getCarId() == null) {
            throw new IllegalArgumentException("车架号不能为空");
        }
        if (vehicle.getBatteryTypeId() <= 0) {
            throw new IllegalArgumentException("电池类型ID必须大于0");
        }

        try {
            // 检查车架号是否已存在
            if (vehicleMapper.existsByCarId(vehicle.getCarId())) {
                throw new IllegalArgumentException("车架号已存在: " + vehicle.getCarId());
            }

            // 生成VID
//            vehicle.setVid(VidGenerator.generateVid());
            // 设置默认值
            vehicle.setDeleted(false);
            vehicle.setTotalMileage(0);
            vehicle.setBatteryHealth(100);
            // 设置创建和更新时间
            LocalDateTime now = LocalDateTime.now();
            vehicle.setCreatedAt(now);
            vehicle.setUpdatedAt(now);

            int result = vehicleMapper.save(vehicle);
            if (result != 1) {
                throw new RuntimeException("创建车辆失败");
            }
            return vehicle;
        } catch (Exception e) {
            log.error("创建车辆时发生错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Vehicle getVehicleByVid(String vid) {
        Vehicle vehicle = vehicleMapper.findById(vid);
        if (vehicle == null) {
            throw new RuntimeException("未找到VID为: " + vid + " 的车辆");
        }
        return vehicle;
    }

    @Override
    public Vehicle getVehicleByCarId(Integer carId) {
        Vehicle vehicle = vehicleMapper.findByCarId(carId);
        if (vehicle == null) {
            throw new RuntimeException("未找到车架号为: " + carId + " 的车辆");
        }
        return vehicle;
    }

    @Override
    public List<Vehicle> getAllVehicles() {
        return vehicleMapper.findAll();
    }

    @Override
    @Transactional
    public Vehicle updateVehicle(Vehicle vehicle) {
        // 检查车辆是否存在
        Vehicle existingVehicle = vehicleMapper.findById(vehicle.getVid());
        if (existingVehicle == null) {
            throw new RuntimeException("未找到VID为: " + vehicle.getVid() + " 的车辆");
        }

        // 只更新允许修改的字段
        existingVehicle.setTotalMileage(vehicle.getTotalMileage());
        existingVehicle.setBatteryHealth(vehicle.getBatteryHealth());
        existingVehicle.setUpdatedAt(LocalDateTime.now());

        // 执行更新
        int result = vehicleMapper.update(existingVehicle);
        if (result != 1) {
            throw new RuntimeException("更新车辆信息失败");
        }
        return existingVehicle;
    }

    @Override
    @Transactional(timeout = 5)
    public void deleteVehicle(String vid) {
        try {
            // 检查车辆是否存在且未删除
            if (!vehicleMapper.existsById(vid)) {
                throw new RuntimeException("未找到VID为: " + vid + " 的车辆");
            }

            // 软删除车辆
            int result = vehicleMapper.softDelete(vid);
            if (result != 1) {
                throw new RuntimeException("删除车辆失败");
            }
        } catch (Exception e) {
            log.error("删除车辆时发生错误: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean existsActiveVehicle(String vid) {
        return vehicleMapper.existsById(vid);
    }

    @Override
    public boolean existsActiveVehicleByCarId(Integer carId) {
        return vehicleMapper.existsByCarId(carId);
    }

    @Override
    @Transactional
    public Vehicle updateVehicleByCarId(Integer carId, Vehicle vehicle) {
        // 检查车辆是否存在
        Vehicle existingVehicle = vehicleMapper.findByCarId(carId);
        if (existingVehicle == null) {
            throw new RuntimeException("未找到车架号为: " + carId + " 的车辆");
        }

        // 只更新允许修改的字段
        existingVehicle.setTotalMileage(vehicle.getTotalMileage());
        existingVehicle.setBatteryHealth(vehicle.getBatteryHealth());
        existingVehicle.setUpdatedAt(LocalDateTime.now());

        // 执行更新
        int result = vehicleMapper.update(existingVehicle);
        if (result != 1) {
            throw new RuntimeException("更新车辆信息失败");
        }
        return existingVehicle;
    }

    @Override
    @Transactional(timeout = 5)
    public void deleteVehicleByCarId(Integer carId) {
        try {
            // 检查车辆是否存在且未删除
            if (!vehicleMapper.existsByCarId(carId)) {
                throw new RuntimeException("未找到车架号为: " + carId + " 的车辆");
            }

            // 软删除车辆
            int result = vehicleMapper.softDeleteByCarId(carId);
            if (result != 1) {
                throw new RuntimeException("删除车辆失败");
            }
        } catch (Exception e) {
            log.error("删除车辆时发生错误: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Map<String, Object> getVehicleWithBatteryType(Integer carId) {
        Map<String, Object> result = vehicleMapper.findVehicleWithBatteryType(carId);
        if (result == null) {
            throw new RuntimeException("未找到车架号为: " + carId + " 的车辆");
        }
        return result;
    }

    @Override
    public String getBatteryTypeName(Integer carId) {
        String typeName = vehicleMapper.findBatteryTypeName(carId);
        if (typeName == null) {
            throw new RuntimeException("未找到车架号为: " + carId + " 的车辆或其电池类型");
        }
        return typeName;
    }

    @Override
    public Integer getBatteryTypeIdByCarId(Integer carId) {
        log.info("开始查询车架号为: {} 的电池类型ID", carId);
        
        if (carId == null) {
            throw new IllegalArgumentException("车架号不能为空");
        }

        Integer batteryTypeId = vehicleMapper.getBatteryTypeIdByCarId(carId);
        if (batteryTypeId == null) {
            log.error("未找到车架号为: {} 的车辆或其电池类型", carId);
            throw new RuntimeException("未找到车架号为: " + carId + " 的车辆或其电池类型");
        }

        log.info("成功获取车架号: {} 的电池类型ID: {}", carId, batteryTypeId);
        return batteryTypeId;
    }

    @Override
    public BatteryType getBatteryTypeByCarId(Integer carId) {
        log.info("开始查询车架号为: {} 的电池类型信息", carId);
        
        if (carId == null) {
            throw new IllegalArgumentException("车架号不能为空");
        }

        BatteryType batteryType = vehicleMapper.getBatteryTypeByCarId(carId);
        if (batteryType == null) {
            log.error("未找到车架号为: {} 的车辆或其电池类型", carId);
            throw new RuntimeException("未找到车架号为: " + carId + " 的车辆或其电池类型");
        }

        log.info("成功获取车架号: {} 的电池类型信息: {}", carId, batteryType);
        return batteryType;
    }

    @Override
    public Integer getCarIdByVid(String vid) {
        log.info("开始查询VID为: {} 的车架号", vid);
        
        if (vid == null || vid.trim().isEmpty()) {
            throw new IllegalArgumentException("VID不能为空");
        }

        Vehicle vehicle = vehicleMapper.findById(vid);
        if (vehicle == null || vehicle.isDeleted()) {
            log.error("未找到VID为: {} 的车辆或车辆已删除", vid);
            throw new RuntimeException("未找到VID为: " + vid + " 的车辆或车辆已删除");
        }

        log.info("成功获取VID: {} 对应的车架号: {}", vid, vehicle.getCarId());
        return vehicle.getCarId();
    }

    @Override
    public Set<String> getVidsByCarIds(@Param("carIds") Set<Integer> carIds) {
        if (carIds == null || carIds.isEmpty()) {
            return Collections.emptySet();
        }
        return vehicleMapper.findVidsByCarIds(carIds);
    }

}
