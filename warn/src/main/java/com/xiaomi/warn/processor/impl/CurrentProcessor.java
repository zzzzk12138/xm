package com.xiaomi.warn.processor.impl;

import com.xiaomi.warn.entity.WarnRule;
import com.xiaomi.warn.mapper.warnMapper;
import com.xiaomi.warn.processor.AbstractSignalProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 电流信号处理器
 */
@Slf4j
@Component
public class CurrentProcessor extends AbstractSignalProcessor {
    
    public CurrentProcessor(warnMapper warnMapper) {
        super(warnMapper);
    }

    @Override
    protected String getMaxKey() {
        return "Ix";
    }

    @Override
    protected String getMinKey() {
        return "Ii";
    }

    @Override
    protected String getLogPrefix() {
        return "电流";
    }

    @Override
    protected WarnRule findWarnRule(int batteryTypeId, BigDecimal currentDiff) {
        WarnRule rule = warnMapper.getCurrentWarnRule(batteryTypeId, currentDiff);
        if (rule == null) {
            log.info("电流差值{}A小于最小报警阈值，不需要报警", currentDiff);
            rule = new WarnRule();
            rule.setWarnLevel((byte) -1);
        }
        return rule;
    }
} 