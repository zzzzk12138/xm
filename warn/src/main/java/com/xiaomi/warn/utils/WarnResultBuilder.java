package com.xiaomi.warn.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 警告结果构建器
 */
public class WarnResultBuilder {
    private final Map<String, Object> result = new HashMap<>();

    public WarnResultBuilder setCarId(Integer carId) {
        result.put("车架编号", carId);
        return this;
    }

    public WarnResultBuilder setBatteryType(String batteryType) {
        result.put("电池类型", batteryType);
        return this;
    }

    public WarnResultBuilder setError(String error) {
        result.put("error", error);
        return this;
    }

    public Map<String, Object> build() {
        return result;
    }
} 