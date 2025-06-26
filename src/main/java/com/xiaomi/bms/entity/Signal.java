package com.xiaomi.bms.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Signal {

    private Long signalId;

    private String vid;

    private BigDecimal maxVoltage;

    private BigDecimal minVoltage;

    private BigDecimal maxCurrent;

    private BigDecimal minCurrent;

    private LocalDateTime recordedAt;

    private boolean isDeleted = false;
} 