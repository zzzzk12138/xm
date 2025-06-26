package com.xiaomi.bms.mapper;

import com.xiaomi.bms.entity.WarnRule;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ruleMapper {
    // 插入新的告警规则
    int insert(WarnRule rule);
    
    // 更新告警规则
    int update(WarnRule rule);
    
    // 根据ID查询告警规则
    WarnRule findById(Integer ruleId);
    
    // 查询所有未删除的告警规则
    List<WarnRule> findAll();
    
    // 根据电池类型ID查询告警规则
    List<WarnRule> findByBatteryTypeId(int batteryTypeId);
    
    // 软删除告警规则
    int softDelete(Integer ruleId);
    
    // 检查规则是否存在
    boolean existsById(Integer ruleId);
    
    // 检查规则代码是否已存在
    boolean existsByRuleCode(int ruleCode);
}
