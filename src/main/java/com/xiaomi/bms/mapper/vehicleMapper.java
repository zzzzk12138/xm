package com.xiaomi.bms.mapper;

import com.xiaomi.bms.entity.Vehicle;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface vehicleMapper {
    /**
     * 保存新车辆
     * @param vehicle 车辆信息
     * @return 影响的行数
     */
    int save(Vehicle vehicle);

    /**
     * 根据VID查询未删除的车辆
     * @param vid 车辆VID
     * @return 车辆信息
     */
    Vehicle findById(String vid);

    /**
     * 根据车架号查询未删除的车辆
     * @param carId 车架号
     * @return 车辆信息
     */
    Vehicle findByCarId(Integer carId);

    /**
     * 查询所有未删除的车辆
     * @return 车辆列表
     */
    List<Vehicle> findAll();

    /**
     * 更新车辆信息
     * @param vehicle 车辆信息
     * @return 影响的行数
     */
    int update(Vehicle vehicle);

    /**
     * 软删除车辆
     * @param vid 车辆VID
     * @return 影响的行数
     */
    int softDelete(String vid);

    /**
     * 检查车辆是否存在且未删除
     * @param vid 车辆VID
     * @return 是否存在且未删除
     */
    boolean existsById(String vid);

    /**
     * 检查车架号是否存在且未删除
     * @param carId 车架号
     * @return 是否存在且未删除
     */
    boolean existsByCarId(Integer carId);

    /**
     * 根据车架号软删除车辆
     * @param carId 车架号
     * @return 影响的行数
     */
    int softDeleteByCarId(Integer carId);

    /**
     * 联合查询车辆信息和电池类型名称
     * @param carId 车架号
     * @return 包含车辆信息和电池类型名称的Map
     */
    Map<String, Object> findVehicleWithBatteryType(@Param("carId") Integer carId);

    /**
     * 根据车架号查询电池类型名称
     * @param carId 车架号
     * @return 电池类型名称
     */
    String findBatteryTypeName(@Param("carId") Integer carId);
}
