package com.xiaomi.warn.processor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaomi.warn.entity.WarnRule;
import com.xiaomi.warn.mapper.warnMapper;
import com.xiaomi.warn.mapper.warnRuleMapper;
import com.xiaomi.warn.processor.AbstractSignalProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 电压信号处理器
 */
@Slf4j
@Component
public class VoltageProcessor extends AbstractSignalProcessor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 按照 key：WARN_RULE_KEY_PREFIX:ruleCode:battery_type_id 存储
    private static final String WARN_RULE_KEY_PREFIX = "warnrule";
    
    public VoltageProcessor(warnRuleMapper warnRuleMapper, warnMapper warnMapper) {
        super(warnRuleMapper, warnMapper);
    }

    @Override
    protected String getMaxKey() {
        return "Mx";
    }

    @Override
    protected String getMinKey() {
        return "Mi";
    }

    @Override
    protected String getLogPrefix() {
        return "电压";
    }

    @Override
    protected WarnRule findWarnRule(int batteryTypeId, BigDecimal voltageDiff) {
        String warnRuleKey = WARN_RULE_KEY_PREFIX + ":1:" + batteryTypeId;
        try {
            List<String> ruleJsonList = stringRedisTemplate.opsForList().range(warnRuleKey, 0, -1);
            if (ruleJsonList != null && !ruleJsonList.isEmpty()) {
                log.info("从Redis中获取到电压告警规则列表: key={}, size={}", warnRuleKey, ruleJsonList.size());
                for (String json : ruleJsonList) {
                    try {
                        WarnRule rule = null;
                        JsonNode node = objectMapper.readTree(json);
                        if (node.isArray() && node.size() == 2) {
                            JsonNode realRuleNode = node.get(1);
                            rule = new WarnRule();
                            rule.setRuleCode(realRuleNode.get("rule_code").asInt());
                            rule.setRuleName(realRuleNode.get("rule_name").asText());
                            rule.setBatteryTypeId(realRuleNode.get("battery_type_id").asInt());
                            rule.setWarnLevel((byte) realRuleNode.get("warn_level").asInt());
                            rule.setDeleted(realRuleNode.get("is_deleted").asBoolean());
                            // min_threshold
                            JsonNode minNode = realRuleNode.get("min_threshold");
                            if (minNode.isArray() && minNode.size() == 2) {
                                rule.setMinThreshold(new java.math.BigDecimal(minNode.get(1).asText()));
                            } else if (minNode.isNumber() || minNode.isTextual()) {
                                rule.setMinThreshold(new java.math.BigDecimal(minNode.asText()));
                            }
                            // max_threshold
                            JsonNode maxNode = realRuleNode.get("max_threshold");
                            if (maxNode.isArray() && maxNode.size() == 2) {
                                rule.setMaxThreshold(new java.math.BigDecimal(maxNode.get(1).asText()));
                            } else if (maxNode.isNumber() || maxNode.isTextual()) {
                                rule.setMaxThreshold(new java.math.BigDecimal(maxNode.asText()));
                            }
                        } else {
                            rule = objectMapper.readValue(json, WarnRule.class);
                        }
                        if (rule != null && !rule.isDeleted() &&
                                voltageDiff.compareTo(rule.getMinThreshold()) >= 0 &&
                                voltageDiff.compareTo(rule.getMaxThreshold()) <= 0) {
                            return rule;
                        }
                    } catch (Exception ex) {
                        log.warn("单条规则反序列化失败，跳过: {}", ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("从Redis获取电压告警规则失败: key={}, error={}", warnRuleKey, e.getMessage(), e);
        }
        WarnRule rule = warnRuleMapper.getVoltageWarnRule(batteryTypeId, voltageDiff);
        if (rule == null) {
            log.info("电压差值{}V小于最小报警阈值，不需要报警", voltageDiff);
            rule = new WarnRule();
            rule.setWarnLevel((byte) -1);
        }
        return rule;
    }
} 