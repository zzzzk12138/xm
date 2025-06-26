package com.xiaomi.warn.service;

import com.xiaomi.warn.entity.Vehicle;
import com.xiaomi.warn.entity.BatteryType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface vehicleService {
    /**
     * 创建新车辆
     * @param vehicle 车辆信息
     * @return 创建后的车辆信息
     */
    Vehicle createVehicle(Vehicle vehicle);

    /**
     * 根据VID查询未删除的车辆
     * @param vid 车辆VID
     * @return 车辆信息
     * @throws RuntimeException 当车辆不存在或已删除时
     */
    Vehicle getVehicleByVid(String vid);

    /**
     * 根据carId查询未删除的车辆
     * @param carId 车架号
     * @return 车辆信息
     * @throws RuntimeException 当车辆不存在或已删除时
     */
    Vehicle getVehicleByCarId(Integer carId);

    /**
     * 查询所有未删除的车辆
     * @return 车辆列表
     */
    List<Vehicle> getAllVehicles();

    /**
     * 更新未删除的车辆信息
     * @param vehicle 更新的车辆信息
     * @return 更新后的车辆信息
     * @throws RuntimeException 当车辆不存在或已删除时
     */
    Vehicle updateVehicle(Vehicle vehicle);

    /**
     * 软删除车辆
     * @param vid 车辆VID
     * @throws RuntimeException 当车辆不存在或已删除时
     */
    void deleteVehicle(String vid);

    /**
     * 检查车辆是否存在且未删除
     * @param vid 车辆VID
     * @return 是否存在且未删除
     */
    boolean existsActiveVehicle(String vid);

    /**
     * 检查指定车架号的车辆是否存在且未删除
     * @param carId 车架号
     * @return 是否存在且未删除
     */
    boolean existsActiveVehicleByCarId(Integer carId);

    /**
     * 根据车架号更新车辆信息
     * @param carId 车架号
     * @param vehicle 更新的车辆信息
     * @return 更新后的车辆信息
     */
    Vehicle updateVehicleByCarId(Integer carId, Vehicle vehicle);

    /**
     * 根据车架号删除车辆（软删除）
     * @param carId 车架号
     */
    void deleteVehicleByCarId(Integer carId);

    /**
     * 根据车架号获取车辆电池类型名称
     * @param carId 车架号
     * @return 包含车辆信息和电池类型名称的Map
     */
    Map<String, Object> getVehicleWithBatteryType(Integer carId);

    /**
     * 根据车架号获取电池类型名称
     * @param carId 车架号
     * @return 电池类型名称
     */
    String getBatteryTypeName(Integer carId);

    /**
     * 根据车架号获取电池类型ID
     * @param carId 车架号
     * @return 电池类型ID
     * @throws RuntimeException 当车辆不存在或已删除时
     */
    Integer getBatteryTypeIdByCarId(Integer carId);

    /**
     * 根据车架号获取完整的电池类型信息
     * @param carId 车架号
     * @return 电池类型信息
     * @throws RuntimeException 当车辆不存在、已删除或电池类型不存在时
     */
    BatteryType getBatteryTypeByCarId(Integer carId);

    /**
     * 根据VID获取车架号
     * @param vid 车辆VID
     * @return 车架号
     * @throws RuntimeException 当车辆不存在或已删除时
     */
    Integer getCarIdByVid(String vid);

    /**
     * 批量查询车辆VID
     * @param carIds 车架号集合
     * @return VID集合
     */
    Set<String> getVidsByCarIds(Set<Integer> carIds);
}
