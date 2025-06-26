package com.xiaomi.warn.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Signal implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("signal_id")
    private Long signalId;

    @JsonProperty("vid")
    private String vid;

    @JsonProperty("max_voltage")
    private BigDecimal maxVoltage;

    @JsonProperty("min_voltage")
    private BigDecimal minVoltage;

    @JsonProperty("max_current")
    private BigDecimal maxCurrent;

    @JsonProperty("min_current")
    private BigDecimal minCurrent;

    @JsonProperty("recorded_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordedAt;

    @JsonProperty("is_deleted")
    private boolean isDeleted = false;
} 