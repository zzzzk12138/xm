package com.xiaomi.warn.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data

public class Warn {

    private Long warnId;

    private int carId;

    private String batTypename;

    private String warnName;

    private int warnLevel;

    private LocalDateTime createdAt;

    private boolean isDeleted = false;
} 