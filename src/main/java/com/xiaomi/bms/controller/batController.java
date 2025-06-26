package com.xiaomi.bms.controller;

import com.xiaomi.bms.entity.BatteryType;
import com.xiaomi.bms.service.batService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bat")
public class batController {

    @Autowired
    private batService batService;

    /**
     * 创建新的电池类型
     * @param batteryType 电池类型信息
     * @return 创建后的电池类型
     */
    @PostMapping("/type")
    public ResponseEntity<BatteryType> createBatteryType(@RequestBody BatteryType batteryType) {
        log.info("batteryType:{}", batteryType);
        try {
            BatteryType createdType = batService.createBatteryType(batteryType);
            return new ResponseEntity<>(createdType, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 根据ID查询电池类型
     * @param id 电池类型ID
     * @return 电池类型信息
     */
    @GetMapping("/type/{id}")
    public ResponseEntity<BatteryType> getBatteryTypeById(@PathVariable("id") int id) {
        try {
            BatteryType batteryType = batService.getBatteryTypeById(id);
            return new ResponseEntity<>(batteryType, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 获取所有未删除的电池类型
     * @return 电池类型列表
     */
    @GetMapping("/types")
    public ResponseEntity<List<BatteryType>> getAllBatteryTypes() {
        log.info("获取所有未删除的电池类型");
        List<BatteryType> types = batService.getAllBatteryTypes();
        return new ResponseEntity<>(types, HttpStatus.OK);
    }

    /**
     * 更新电池类型信息
     * @param id 电池类型ID
     * @param batteryType 更新的电池类型信息
     * @return 更新后的电池类型
     */
    @PutMapping("/type/{id}")
    public ResponseEntity<BatteryType> updateBatteryType(
            @PathVariable("id") int id,
            @RequestBody BatteryType batteryType) {
        log.info("更新id为:{} 的电池信息为: {}", id, batteryType);
        try {
            batteryType.setId(id);
            BatteryType updatedType = batService.updateBatteryType(batteryType);
            return new ResponseEntity<>(updatedType, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 删除电池类型（软删除）
     * @param id 电池类型ID
     * @return 删除操作的响应状态
     */
    @DeleteMapping("/type/{id}")
    public ResponseEntity<Void> deleteBatteryType(@PathVariable("id") int id) {
        log.info("软删除id为{} 的电池", id);
        try {
            batService.deleteBatteryType(id);
            log.info("删除成功:{}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 检查电池类型是否存在且未删除
     * @param id 电池类型ID
     * @return 是否存在且未删除的状态
     */
    @GetMapping("/type/{id}/exists")
    public ResponseEntity<Boolean> checkBatteryTypeExists(@PathVariable("id") int id) {
        boolean exists = batService.existsActiveBatteryType(id);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }
}
