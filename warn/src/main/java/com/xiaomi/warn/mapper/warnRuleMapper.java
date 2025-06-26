package com.xiaomi.warn.mapper;

import com.xiaomi.warn.entity.WarnRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface warnRuleMapper {
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
}
