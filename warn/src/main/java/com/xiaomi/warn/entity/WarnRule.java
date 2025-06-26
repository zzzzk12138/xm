package com.xiaomi.warn.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WarnRule {
    @JsonProperty("rule_id")
    private Integer ruleId;

    @JsonProperty("rule_code")
    private int ruleCode;

    @JsonProperty("rule_name")
    private String ruleName;

    @JsonProperty("battery_type_id")
    private int batteryTypeId;

    @JsonProperty("min_threshold")
    private BigDecimal minThreshold;

    @JsonProperty("max_threshold")
    private BigDecimal maxThreshold;

    @JsonProperty("warn_level")
    private Byte warnLevel;

    @JsonProperty("is_deleted")
    private boolean isDeleted = false;

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
} 