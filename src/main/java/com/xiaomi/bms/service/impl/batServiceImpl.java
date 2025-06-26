package com.xiaomi.bms.service.impl;

import com.xiaomi.bms.entity.BatteryType;
import com.xiaomi.bms.mapper.batMapper;
import com.xiaomi.bms.service.batService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Service
public class batServiceImpl implements batService {

    @Autowired
    private batMapper batMapper;

    @Override
    @Transactional
    public BatteryType createBatteryType(BatteryType batteryType) {
        // 检查类型名称是否已存在
//        BatteryType existingType = batMapper.selectByTypeName(batteryType.getTypeName());
//        if (existingType != null && !existingType.isDelete()) {
//            throw new RuntimeException("电池类型名称已存在: " + batteryType.getTypeName());
//        }

        // 设置默认值
        batteryType.setDeleted(false);

        // 保存电池类型
        System.out.println(batteryType);
        batMapper.insert(batteryType);
        return batteryType;
    }

    @Override
    public BatteryType getBatteryTypeById(int id) {
        BatteryType batteryType = batMapper.selectById(id);
        if (batteryType == null || batteryType.isDeleted()) {
            throw new RuntimeException("未找到ID为: " + id + " 的电池类型");
        }
        return batteryType;
    }

    @Override
    public List<BatteryType> getAllBatteryTypes() {
        return batMapper.selectAll();
    }

    @Override
    @Transactional
    public BatteryType updateBatteryType(BatteryType batteryType) {
        // 检查是否存在
        if (!batMapper.existsAndNotDeleted(batteryType.getId())) {
            throw new RuntimeException("未找到ID为: " + batteryType.getId() + " 的电池类型");
        }

        // 检查新名称是否与其他类型重复
        BatteryType existingType = batMapper.selectByTypeName(batteryType.getTypeName());
        if (existingType != null && !existingType.isDeleted() && existingType.getId() != batteryType.getId()) {
            throw new RuntimeException("电池类型名称已存在: " + batteryType.getTypeName());
        }

        // 保持删除状态不变
        BatteryType currentType = batMapper.selectById(batteryType.getId());
        batteryType.setDeleted(currentType.isDeleted());

        // 更新电池类型
        batMapper.update(batteryType);
        return batteryType;
    }

    @Override
    @Transactional(timeout = 5)
    public void deleteBatteryType(int id) {
        try {
            // 检查是否存在
            if (!batMapper.existsAndNotDeleted(id)) {
                throw new RuntimeException("未找到ID为: " + id + " 的电池类型");
            }

            // 软删除电池类型
            int result = batMapper.softDelete(id);
            if (result != 1) {
                throw new RuntimeException("删除电池类型失败");
            }
            
        } catch (Exception e) {
            log.error("删除电池类型时发生错误: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean existsActiveBatteryType(int id) {
        return batMapper.existsAndNotDeleted(id);
    }
}
