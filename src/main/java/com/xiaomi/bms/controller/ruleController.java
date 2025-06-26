package com.xiaomi.bms.controller;

import com.xiaomi.bms.entity.WarnRule;
import com.xiaomi.bms.service.ruleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/rule")
public class ruleController {

    @Autowired
    private ruleService ruleService;

    /**
     * 创建新的告警规则
     * @param rule 告警规则对象
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<?> createRule(@RequestBody WarnRule rule) {
        log.info("接收到创建告警规则请求: {}", rule);
        try {
            boolean success = ruleService.createRule(rule);
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "告警规则创建成功");
                response.put("data", rule);
                return ResponseEntity.ok(response);
            } else {
                log.warn("告警规则创建失败: {}", rule);
                return ResponseEntity.badRequest().body("创建告警规则失败");
            }
        } catch (IllegalArgumentException e) {
            log.warn("创建告警规则参数验证失败: {}, 规则信息: {}", e.getMessage(), rule);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("创建告警规则时发生错误: {}, 规则信息: {}", e.getMessage(), rule, e);
            return ResponseEntity.badRequest().body("创建告警规则时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 更新告警规则
     * @param ruleId 规则ID
     * @param rule 告警规则对象
     * @return 更新结果
     */
    @PutMapping("/{ruleId}")
    public ResponseEntity<?> updateRule(@PathVariable Integer ruleId, @RequestBody WarnRule rule) {
        log.info("开始更新告警规则, ID: {}, 规则信息: {}", ruleId, rule);
        try {
            rule.setRuleId(ruleId);
            boolean success = ruleService.updateRule(rule);
            if (success) {
                log.info("告警规则更新成功, ID: {}", ruleId);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "告警规则更新成功");
                response.put("data", rule);
                return ResponseEntity.ok(response);
            } else {
                log.warn("告警规则更新失败, ID: {}", ruleId);
                return ResponseEntity.badRequest().body("更新告警规则失败");
            }
        } catch (IllegalArgumentException e) {
            log.warn("更新告警规则参数验证失败: {}, ID: {}, 规则信息: {}", e.getMessage(), ruleId, rule);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("更新告警规则时发生错误: {}, ID: {}, 规则信息: {}", e.getMessage(), ruleId, rule, e);
            return ResponseEntity.badRequest().body("更新告警规则时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询告警规则
     * @param ruleId 规则ID
     * @return 告警规则对象
     */
    @GetMapping("/{ruleId}")
    public ResponseEntity<?> getRule(@PathVariable Integer ruleId) {
        log.info("开始查询告警规则, ID: {}", ruleId);
        try {
            WarnRule rule = ruleService.getRuleById(ruleId);
            if (rule != null) {
                log.info("告警规则查询成功, ID: {}, 规则信息: {}", ruleId, rule);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "告警规则查询成功");
                response.put("data", rule);
                return ResponseEntity.ok(response);
            } else {
                log.warn("未找到告警规则, ID: {}", ruleId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("查询告警规则时发生错误: {}, ID: {}", e.getMessage(), ruleId, e);
            return ResponseEntity.badRequest().body("查询告警规则时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 查询所有未删除的告警规则
     * @return 告警规则列表
     */
    @GetMapping
    public ResponseEntity<?> getAllRules() {
        log.info("开始查询所有告警规则");
        try {
            List<WarnRule> rules = ruleService.getAllRules();
            log.info("查询到 {} 条告警规则", rules.size());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "查询所有告警规则成功");
            response.put("data", rules);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询所有告警规则时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("查询告警规则时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 根据电池类型ID查询告警规则
     * @param batteryTypeId 电池类型ID
     * @return 告警规则列表
     */
    @GetMapping("/battery/{batteryTypeId}")
    public ResponseEntity<?> getRulesByBatteryType(@PathVariable int batteryTypeId) {
        log.info("开始查询电池类型的告警规则, 电池类型ID: {}", batteryTypeId);
        try {
            List<WarnRule> rules = ruleService.getRulesByBatteryTypeId(batteryTypeId);
            log.info("查询到电池类型 {} 的告警规则 {} 条", batteryTypeId, rules.size());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "查询电池类型告警规则成功");
            response.put("data", rules);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("根据电池类型查询告警规则时发生错误: {}, 电池类型ID: {}", e.getMessage(), batteryTypeId, e);
            return ResponseEntity.badRequest().body("查询告警规则时发生系统错误: " + e.getMessage());
        }
    }

    /**
     * 删除告警规则
     * @param ruleId 规则ID
     * @return 删除结果
     */
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<?> deleteRule(@PathVariable Integer ruleId) {
        log.info("开始删除告警规则, ID: {}", ruleId);
        try {
            boolean success = ruleService.deleteRule(ruleId);
            if (success) {
                log.info("告警规则删除成功, ID: {}", ruleId);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "告警规则删除成功");
                return ResponseEntity.ok(response);
            } else {
                log.warn("告警规则删除失败, ID: {}", ruleId);
                return ResponseEntity.badRequest().body("删除告警规则失败");
            }
        } catch (Exception e) {
            log.error("删除告警规则时发生错误: {}, ID: {}", e.getMessage(), ruleId, e);
            return ResponseEntity.badRequest().body("删除告警规则时发生系统错误: " + e.getMessage());
        }
    }
}
