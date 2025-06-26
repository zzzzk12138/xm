package com.xiaomi.warn.mapper;

import com.xiaomi.warn.entity.Warn;
import com.xiaomi.warn.entity.WarnRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.Map;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface warnMapper {
//    /**
//     * 保存警告信息
//     * @param carId 车架编号
//     * @param warnId 警告规则编号
//     * @param signal 信号数据
//     * @return 影响的行数
//     */
//    int saveWarn(@Param("carId") Integer carId,
//                 @Param("warnId") Integer warnId,
//                 @Param("signal") String signal);

//    /**
//     * 获取警告规则
//     * @param warnId 警告规则编号
//     * @return 警告规则信息
//     */
//    Map<String, Object> getWarnRule(@Param("warnId") Integer warnId);

    /**
     * 根据电池类型和电压差值获取电压预警规则
     * @param batteryTypeId 电池类型ID
     * @param diff 电压差值
     * @return 预警规则
     */
    @Select("SELECT * FROM warn_rule " +
            "WHERE rule_code = 1 " +  // 1代表电压差报警
            "AND battery_type_id = #{batteryTypeId} " +
            "AND #{diff} >= min_threshold " +
            "AND #{diff} <= max_threshold " +
            "AND is_deleted = false")
    WarnRule getVoltageWarnRule(@Param("batteryTypeId") int batteryTypeId, 
                               @Param("diff") BigDecimal diff);

    /**
     * 根据电池类型和电流差值获取电流预警规则
     * @param batteryTypeId 电池类型ID
     * @param diff 电流差值
     * @return 预警规则
     */
    @Select("SELECT * FROM warn_rule " +
            "WHERE rule_code = 2 " +  // 2代表电流差报警
            "AND battery_type_id = #{batteryTypeId} " +
            "AND #{diff} >= min_threshold " +
            "AND #{diff} <= max_threshold " +
            "AND is_deleted = false")
    WarnRule getCurrentWarnRule(@Param("batteryTypeId") int batteryTypeId, 
                               @Param("diff") BigDecimal diff);

    /**
     * 保存预警信息
     * @param carId 车辆ID
     * @param batTypename 电池类型名称
     * @param warnName 预警名称
     * @param warnLevel 预警等级
     * @return 影响的行数
     */
    int saveWarn(@Param("carId") Long carId,
                 @Param("batTypename") String batTypename,
                 @Param("warnName") String warnName,
                 @Param("warnLevel") byte warnLevel);

    /**
     * 查询指定车辆的预警信息
     * @param carId 车辆ID
     * @return 预警信息列表
     */
    List<Warn> findWarnsByCarId(@Param("carId") int carId);
}
