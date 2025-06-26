package com.xiaomi.warn.service.Impl;

import com.xiaomi.warn.entity.WarnRule;
import com.xiaomi.warn.mapper.warnRuleMapper;
import com.xiaomi.warn.service.warnRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class warnRuleServiceImpl implements warnRuleService {

    @Autowired
    private warnRuleMapper warnRuleMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    // 按照 key：WARN_RULE_KEY_PREFIX:ruleCode:battery_type_id 存储
    private static final String WARN_RULE_KEY_PREFIX = "warnrule:";

    /**
     * 获取电压差报警规则
     * @param batteryTypeId 电池类型ID
     * @param voltageDiff 电压差值
     * @return 报警规则，如果差值太小返回level为-1的规则
     */
    @Override
    public WarnRule getVoltageWarnRule(int batteryTypeId, BigDecimal voltageDiff) {
        String warnRuleKey = WARN_RULE_KEY_PREFIX + ":1:" + batteryTypeId;
        
        try {
            // 从Redis中获取规则列表
            List<Object> ruleList = redisTemplate.opsForList().range(warnRuleKey, 0, -1);
            
            if (ruleList != null && !ruleList.isEmpty()) {
                log.info("从Redis中获取到电压告警规则列表: key={}, size={}", warnRuleKey, ruleList.size());
                
                // 遍历规则列表，找到匹配的规则
                for (Object obj : ruleList) {
                    if (obj instanceof List && ((List<?>) obj).size() == 2) {
                        List<?> ruleData = (List<?>) obj;
                        // 第二个元素是规则的实际数据
                        Object ruleObj = ruleData.get(1);
                        if (ruleObj instanceof WarnRule) {
                            WarnRule rule = (WarnRule) ruleObj;
                            if (!rule.isDeleted() && 
                                rule.getMinThreshold().compareTo(voltageDiff) <= 0 && 
                                rule.getMaxThreshold().compareTo(voltageDiff) >= 0) {
                                return rule;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("从Redis获取电压告警规则失败: key={}, error={}", warnRuleKey, e.getMessage(), e);
        }
        
        // 如果Redis中没有找到或发生异常，从数据库查询
        WarnRule rule = warnRuleMapper.getVoltageWarnRule(batteryTypeId, voltageDiff);
        if (rule == null) {
            log.info("电压差值{}V小于最小报警阈值，不需要报警", voltageDiff);
            rule = new WarnRule();
            rule.setWarnLevel((byte) -1);
        }
        return rule;
    }

    /**
     * 获取电流差报警规则
     * @param batteryTypeId 电池类型ID
     * @param currentDiff 电流差值
     * @return 报警规则，如果差值太小返回level为-1的规则
     */
    @Override
    public WarnRule getCurrentWarnRule(int batteryTypeId, BigDecimal currentDiff) {
        String warnRuleKey = WARN_RULE_KEY_PREFIX + ":2:" + batteryTypeId;
        
        try {
            // 从Redis中获取规则列表
            List<Object> ruleList = redisTemplate.opsForList().range(warnRuleKey, 0, -1);
            
            if (ruleList != null && !ruleList.isEmpty()) {
                log.info("从Redis中获取到电流告警规则列表: key={}, size={}", warnRuleKey, ruleList.size());
                
                // 遍历规则列表，找到匹配的规则
                for (Object obj : ruleList) {
                    if (obj instanceof List && ((List<?>) obj).size() == 2) {
                        List<?> ruleData = (List<?>) obj;
                        // 第二个元素是规则的实际数据
                        Object ruleObj = ruleData.get(1);
                        if (ruleObj instanceof WarnRule) {
                            WarnRule rule = (WarnRule) ruleObj;
                            if (!rule.isDeleted() && 
                                rule.getMinThreshold().compareTo(currentDiff) <= 0 && 
                                rule.getMaxThreshold().compareTo(currentDiff) >= 0) {
                                return rule;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("从Redis获取电流告警规则失败: key={}, error={}", warnRuleKey, e.getMessage(), e);
        }
        
        // 如果Redis中没有找到或发生异常，从数据库查询
        WarnRule rule = warnRuleMapper.getCurrentWarnRule(batteryTypeId, currentDiff);
        if (rule == null) {
            log.info("电流差值{}A小于最小报警阈值，不需要报警", currentDiff);
            rule = new WarnRule();
            rule.setWarnLevel((byte) -1);
        }
        return rule;
    }
}
