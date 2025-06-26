package com.xiaomi.warn.processor;

import com.xiaomi.warn.dto.warnDTO;
import com.xiaomi.warn.entity.BatteryType;
import java.util.Map;

/**
 * 信号处理器接口
 */
public interface SignalProcessor {
    /**
     * 处理信号数据
     * @param warn 警告DTO
     * @param batteryType 电池类型
     * @param signalMap 信号数据
     * @return 处理结果
     */
    Map<String, Object> process(warnDTO warn, BatteryType batteryType, Map<String, Double> signalMap);
} 