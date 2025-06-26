package com.xiaomi.bms.service;

import com.xiaomi.bms.entity.WarnRule;
import java.util.List;

public interface ruleService {
    /**
     * 创建新的告警规则
     * @param rule 告警规则对象
     * @return 创建成功返回true，否则返回false
     */
    boolean createRule(WarnRule rule);

    /**
     * 更新告警规则
     * @param rule 告警规则对象
     * @return 更新成功返回true，否则返回false
     */
    boolean updateRule(WarnRule rule);

    /**
     * 根据ID查询告警规则
     * @param ruleId 规则ID
     * @return 告警规则对象，如果不存在返回null
     */
    WarnRule getRuleById(Integer ruleId);

    /**
     * 查询所有未删除的告警规则
     * @return 告警规则列表
     */
    List<WarnRule> getAllRules();

    /**
     * 根据电池类型ID查询告警规则
     * @param batteryTypeId 电池类型ID
     * @return 告警规则列表
     */
    List<WarnRule> getRulesByBatteryTypeId(int batteryTypeId);

    /**
     * 软删除告警规则
     * @param ruleId 规则ID
     * @return 删除成功返回true，否则返回false
     */
    boolean deleteRule(Integer ruleId);
}
