package com.xiaomi.bms.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Vehicle {
    @JsonProperty("vid")
    private String vid;

    @JsonProperty("car_id")
    private Integer carId;

    @JsonProperty("battery_type_id")
    private int batteryTypeId;

    @JsonProperty("total_mileage")
    private Integer totalMileage = 0;

    @JsonProperty("battery_health")
    private int batteryHealth = 100;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("is_deleted")
    private boolean isDeleted = false;

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
} 