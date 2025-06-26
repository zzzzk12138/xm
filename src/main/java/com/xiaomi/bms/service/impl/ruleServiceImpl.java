package com.xiaomi.bms.service.impl;

import com.xiaomi.bms.entity.WarnRule;
import com.xiaomi.bms.mapper.ruleMapper;
import com.xiaomi.bms.service.ruleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
@Service
public class ruleServiceImpl implements ruleService {
    
    @Autowired
    private ruleMapper ruleMapper;

    @Override
    @Transactional
    public boolean createRule(WarnRule rule) {
        log.info("开始处理告警规则创建请求: {}", rule);
        
        // 参数验证
        if (rule == null) {
            throw new IllegalArgumentException("告警规则不能为空");
        }
        if (rule.getRuleName() == null || rule.getRuleName().trim().isEmpty()) {
            throw new IllegalArgumentException("规则名称不能为空");
        }
        if (rule.getBatteryTypeId() <= 0) {
            throw new IllegalArgumentException("电池类型ID必须大于0");
        }
        if (rule.getMinThreshold() == null && rule.getMaxThreshold() == null) {
            throw new IllegalArgumentException("最小阈值和最大阈值不能同时为空");
        }
        if (rule.getWarnLevel() == null) {
            throw new IllegalArgumentException("告警等级不能为空");
        }
        
        // 设置默认值
        rule.setDeleted(false);
        
        // 执行插入操作
        boolean result = ruleMapper.insert(rule) > 0;
        log.info("告警规则创建{}: {}", result ? "成功" : "失败", rule);
        return result;
    }

    @Override
    @Transactional
    public boolean updateRule(WarnRule rule) {
        // 检查规则是否存在
        if (!ruleMapper.existsById(rule.getRuleId())) {
            return false;
        }
        
        return ruleMapper.update(rule) > 0;
    }

    @Override
    public WarnRule getRuleById(Integer ruleId) {
        return ruleMapper.findById(ruleId);
    }

    @Override
    public List<WarnRule> getAllRules() {
        return ruleMapper.findAll();
    }

    @Override
    public List<WarnRule> getRulesByBatteryTypeId(int batteryTypeId) {
        return ruleMapper.findByBatteryTypeId(batteryTypeId);
    }

    @Override
    @Transactional
    public boolean deleteRule(Integer ruleId) {
        // 检查规则是否存在
        if (!ruleMapper.existsById(ruleId)) {
            return false;
        }
        
        return ruleMapper.softDelete(ruleId) > 0;
    }
}
