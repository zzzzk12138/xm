package com.xiaomi.warn.service;

import com.xiaomi.warn.entity.WarnRule;

import java.math.BigDecimal;

public interface warnRuleService {

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
