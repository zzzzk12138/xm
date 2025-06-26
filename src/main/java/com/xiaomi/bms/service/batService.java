package com.xiaomi.bms.service;

import com.xiaomi.bms.entity.BatteryType;
import java.util.List;

public interface batService {
    /**
     * 创建新的电池类型
     * @param batteryType 电池类型信息
     * @return 创建后的电池类型
     */
    BatteryType createBatteryType(BatteryType batteryType);

    /**
     * 根据ID查询电池类型
     * @param id 电池类型ID
     * @return 电池类型信息
     */
    BatteryType getBatteryTypeById(int id);

    /**
     * 获取所有未删除的电池类型
     * @return 电池类型列表
     */
    List<BatteryType> getAllBatteryTypes();

    /**
     * 更新电池类型信息
     * @param batteryType 更新的电池类型信息
     * @return 更新后的电池类型
     */
    BatteryType updateBatteryType(BatteryType batteryType);

    /**
     * 删除电池类型（软删除）
     * @param id 电池类型ID
     */
    void deleteBatteryType(int id);

    /**
     * 检查电池类型是否存在且未删除
     * @param id 电池类型ID
     * @return 是否存在且未删除
     */
    boolean existsActiveBatteryType(int id);
}
