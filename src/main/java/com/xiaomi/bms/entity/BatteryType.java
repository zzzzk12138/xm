package com.xiaomi.bms.entity;

import lombok.Data;

@Data
public class BatteryType {

    private int id;

    private String typeName;

    private boolean isDeleted = false;
} 