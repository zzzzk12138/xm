package com.xiaomi.warn.service;

import com.xiaomi.warn.dto.warnDTO;
import com.xiaomi.warn.entity.WarnRule;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface warnService {
    /**
     * 批量处理警告信息
     * @param warnDTOList 警告信息列表
     * @return 处理结果列表
     */
    List<Map<String, Object>> processWarns(List<warnDTO> warnDTOList);

    /**
     * 查询指定车辆的预警信息
     * @param carId 车辆ID
     * @return 预警信息列表
     */
    List<Map<String, Object>> getWarnsByCarId(Integer carId);

    /**
     * 根据电池类型和电压差值获取电压预警规则
     * @param batteryTypeId 电池类型ID
     * @param voltageDiff 电压差值
     * @return 预警规则
     */
    WarnRule getVoltageWarnRule(int batteryTypeId, BigDecimal voltageDiff);

    /**
     * 根据电池类型和电流差值获取电流预警规则
     * @param batteryTypeId 电池类型ID
     * @param currentDiff 电流差值
     * @return 预警规则
     */
    WarnRule getCurrentWarnRule(int batteryTypeId, BigDecimal currentDiff);
}
