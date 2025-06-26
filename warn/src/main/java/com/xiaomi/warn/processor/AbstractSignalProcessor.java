package com.xiaomi.warn.processor;

import com.xiaomi.warn.dto.warnDTO;
import com.xiaomi.warn.entity.BatteryType;
import com.xiaomi.warn.entity.WarnRule;
import com.xiaomi.warn.mapper.warnMapper;
import com.xiaomi.warn.mapper.warnRuleMapper;
import com.xiaomi.warn.utils.WarnResultBuilder;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 抽象信号处理器 - 实现通用的处理逻辑
 */
@Slf4j
public abstract class AbstractSignalProcessor implements SignalProcessor {
    
    protected final warnMapper warnMapper;
    protected final warnRuleMapper warnRuleMapper;
    
    protected AbstractSignalProcessor(warnRuleMapper warnRuleMapper, warnMapper warnMapper) {
        this.warnRuleMapper = warnRuleMapper;
        this.warnMapper = warnMapper;
    }

    protected abstract String getMaxKey();
    protected abstract String getMinKey();
    protected abstract String getLogPrefix();
    protected abstract WarnRule findWarnRule(int batteryTypeId, BigDecimal diff);

    @Override
    public Map<String, Object> process(warnDTO warn, BatteryType batteryType, Map<String, Double> signalMap) {
        if (!signalMap.containsKey(getMaxKey()) || !signalMap.containsKey(getMinKey())) {
            return null;
        }

        Map<String, Object> result = new WarnResultBuilder()
                .setCarId(warn.getCarId())
                .setBatteryType(batteryType.getTypeName())
                .build();

        BigDecimal diff = BigDecimal.valueOf(signalMap.get(getMaxKey()) - signalMap.get(getMinKey()));
        WarnRule rule = findWarnRule(batteryType.getId(), diff);

        if (rule != null) {
            result.put("warnName", rule.getRuleName());
            result.put("warnLevel", rule.getWarnLevel());

            if (rule.getWarnLevel() != -1) {
                saveWarnIfNeeded(warn.getCarId().longValue(), batteryType.getTypeName(), rule);
            }
            return result;
        }
        return null;
    }

    protected void saveWarnIfNeeded(Long carId, String batteryTypeName, WarnRule rule) {
        try {
            warnMapper.saveWarn(carId, batteryTypeName, rule.getRuleName(), rule.getWarnLevel());
        } catch (Exception e) {
            log.error("保存{}预警信息失败: {}", getLogPrefix(), e.getMessage(), e);
        }
    }
} 