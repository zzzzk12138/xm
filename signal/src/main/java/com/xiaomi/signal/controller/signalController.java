package com.xiaomi.signal.controller;

import com.xiaomi.signal.entity.Signal;
import com.xiaomi.signal.service.signalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/signal")
public class signalController {
    
    @Autowired
    private signalService signalService;

    /**
     * 创建信号记录
     */
    @PostMapping
    public ResponseEntity<?> createSignal(@RequestBody Signal signal) {
        log.info("接收到创建信号记录请求: {}", signal);
        /**
         * 在实际应用中，电压一般会在360V到420V之间变动，这能保证车辆的动力需求同时维持安全性和稳定性。
         * 假设电池电压为300多伏特，计算得到的电流大约是20几安培。而在加速时，电流可能会增加到这个值的3到5倍。
         */
        /**
         * 目前三辆车的vid
         * VH6FTQ5CHDZOEXEI
         * VHIKR4B9KO8O3OCT
         * VHL3C6TDUXFM6HV6
         * VHIF05TZ1WMONZ52
         * VHOXMWGL8MJ73MCL
         */
        try {
            Signal createdSignal = signalService.createSignal(signal);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "信号记录创建成功");
            response.put("data", createdSignal);
            log.info("信号记录创建成功: {}", createdSignal);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("创建信号记录参数验证失败: {}, 信号数据: {}", e.getMessage(), signal);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("创建信号记录时发生错误: {}, 信号数据: {}", e.getMessage(), signal, e);
            return ResponseEntity.badRequest().body("创建信号记录时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取信号记录
     */
    @GetMapping("/{signalId}")
    public ResponseEntity<?> getSignal(@PathVariable Long signalId) {
        log.info("开始查询信号记录, ID: {}", signalId);
        try {
            Signal signal = signalService.getSignalById(signalId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "信号记录查询成功");
            response.put("data", signal);
            log.info("信号记录查询成功: {}", signal);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("查询信号记录失败: {}, ID: {}", e.getMessage(), signalId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("查询信号记录时发生错误: {}, ID: {}", e.getMessage(), signalId, e);
            return ResponseEntity.badRequest().body("查询信号记录时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 根据VID获取信号记录列表
     */
    @GetMapping("/vehicle/{vid}")
    public ResponseEntity<?> getSignalsByVid(@PathVariable String vid) {
        log.info("开始查询车辆信号记录, VID: {}", vid);
        try {
            List<Signal> signals = signalService.getSignalsByVid(vid);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "车辆信号记录查询成功");
            response.put("data", signals);
            log.info("车辆信号记录查询成功, VID: {}, 记录数: {}", vid, signals.size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("查询车辆信号记录参数验证失败: {}, VID: {}", e.getMessage(), vid);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("查询车辆信号记录时发生错误: {}, VID: {}", e.getMessage(), vid, e);
            return ResponseEntity.badRequest().body("查询车辆信号记录时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 获取所有信号记录
     */
    @GetMapping
    public ResponseEntity<?> getAllSignals() {
        log.info("开始查询所有信号记录");
        try {
            List<Signal> signals = signalService.getAllSignals();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "查询所有信号记录成功");
            response.put("data", signals);
            log.info("查询到 {} 条信号记录", signals.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询所有信号记录时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("查询信号记录时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 更新信号记录
     */
    @PutMapping("/{signalId}")
    public ResponseEntity<?> updateSignal(
            @PathVariable Long signalId,
            @RequestBody Signal signal) {
        log.info("开始更新信号记录, ID: {}, 信号数据: {}", signalId, signal);
        try {
            signal.setSignalId(signalId);
            Signal updatedSignal = signalService.updateSignal(signal);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "信号记录更新成功");
            response.put("data", updatedSignal);
            log.info("信号记录更新成功: {}", updatedSignal);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("更新信号记录失败: {}, ID: {}", e.getMessage(), signalId);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("更新信号记录时发生错误: {}, ID: {}, 信号数据: {}", e.getMessage(), signalId, signal, e);
            return ResponseEntity.badRequest().body("更新信号记录时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 删除信号记录
     */
    @DeleteMapping("/{signalId}")
    public ResponseEntity<?> deleteSignal(@PathVariable Long signalId) {
        log.info("开始删除信号记录, ID: {}", signalId);
        try {
            signalService.deleteSignal(signalId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "信号记录删除成功");
            log.info("信号记录删除成功, ID: {}", signalId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("删除信号记录失败: {}, ID: {}", e.getMessage(), signalId);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("删除信号记录时发生错误: {}, ID: {}", e.getMessage(), signalId, e);
            return ResponseEntity.badRequest().body("删除信号记录时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 删除车辆所有信号记录
     */
    @DeleteMapping("/vehicle/{vid}")
    public ResponseEntity<?> deleteSignalsByVid(@PathVariable String vid) {
        log.info("开始删除车辆所有信号记录, VID: {}", vid);
        try {
            signalService.deleteSignalsByVid(vid);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "车辆信号记录删除成功");
            log.info("车辆信号记录删除成功, VID: {}", vid);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("删除车辆信号记录参数验证失败: {}, VID: {}", e.getMessage(), vid);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("删除车辆信号记录时发生错误: {}, VID: {}", e.getMessage(), vid, e);
            return ResponseEntity.badRequest().body("删除车辆信号记录时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 检查信号记录是否存在
     */
    @GetMapping("/{signalId}/exists")
    public ResponseEntity<?> existsSignal(@PathVariable Long signalId) {
        log.info("开始检查信号记录是否存在, ID: {}", signalId);
        try {
            boolean exists = signalService.existsSignal(signalId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "检查信号记录存在状态成功");
            response.put("data", exists);
            log.info("信号记录存在状态检查完成, ID: {}, exists: {}", signalId, exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("检查信号记录是否存在时发生错误: {}, ID: {}", e.getMessage(), signalId, e);
            return ResponseEntity.badRequest().body("检查信号记录是否存在时发生系统错误: " + e.getMessage());
        }
    }
}
