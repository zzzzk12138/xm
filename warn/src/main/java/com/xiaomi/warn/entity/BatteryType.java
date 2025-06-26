package com.xiaomi.warn.entity;

import lombok.Data;

@Data
public class BatteryType {

    private int id;

    private String typeName;

    private boolean isDeleted = false;
} 