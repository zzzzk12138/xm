package com.xiaomi.warn.mapper;

import com.xiaomi.warn.entity.Vehicle;
import com.xiaomi.warn.entity.BatteryType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * 根据车架号获取电池类型ID
     * @param carId 车架号
     * @return 电池类型ID
     */
    @Select("SELECT battery_type_id FROM vehicle WHERE car_id = #{carId} AND is_deleted = false")
    Integer getBatteryTypeIdByCarId(@Param("carId") Integer carId);

    /**
     * 根据车架号获取完整的电池类型信息
     * @param carId 车架号
     * @return 电池类型信息
     */
    @Select("SELECT bt.* FROM vehicle v " +
            "JOIN battery_type bt ON v.battery_type_id = bt.id " +
            "WHERE v.car_id = #{carId} AND v.is_deleted = false AND bt.is_deleted = false")
    BatteryType getBatteryTypeByCarId(@Param("carId") Integer carId);

    /**
     * 根据carIds批量查询 vids
     * @param carIds 车架号集合
     * @return VID集合
     */
    Set<String> findVidsByCarIds(@Param("carIds") Set<Integer> carIds);
}
