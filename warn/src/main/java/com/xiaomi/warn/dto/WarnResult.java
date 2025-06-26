package com.xiaomi.warn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class WarnResult {
    @JsonProperty("车架编号")
    private Integer carId;

    @JsonProperty("电池类型")
    private String batteryType;

    @JsonProperty("warnName")
    private String warnName;

    @JsonProperty("warnLevel")
    private Integer warnLevel;
} 