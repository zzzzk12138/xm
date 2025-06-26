package com.xiaomi.signal.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime recordedAt;

    /**
     * 状态: 0=正常, 1=已删除, 2=其他状态
     */
    @JsonProperty("status")
    private int status = 0;
} 