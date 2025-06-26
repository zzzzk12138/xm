package com.xiaomi.warn.processor.impl;

import com.xiaomi.warn.entity.WarnRule;
import com.xiaomi.warn.mapper.warnMapper;
import com.xiaomi.warn.processor.AbstractSignalProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 电压信号处理器
 */
@Slf4j
@Component
public class VoltageProcessor extends AbstractSignalProcessor {
    
    public VoltageProcessor(warnMapper warnMapper) {
        super(warnMapper);
    }

    @Override
    protected String getMaxKey() {
        return "Mx";
    }

    @Override
    protected String getMinKey() {
        return "Mi";
    }

    @Override
    protected String getLogPrefix() {
        return "电压";
    }

    @Override
    protected WarnRule findWarnRule(int batteryTypeId, BigDecimal voltageDiff) {
        WarnRule rule = warnMapper.getVoltageWarnRule(batteryTypeId, voltageDiff);
        if (rule == null) {
            log.info("电压差值{}V小于最小报警阈值，不需要报警", voltageDiff);
            rule = new WarnRule();
            rule.setWarnLevel((byte) -1);
        }
        return rule;
    }
} 