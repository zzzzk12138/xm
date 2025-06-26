package com.xiaomi.warn.processor.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.xiaomi.warn.entity.WarnRule;
import com.xiaomi.warn.mapper.warnMapper;
import com.xiaomi.warn.mapper.warnRuleMapper;
import com.xiaomi.warn.processor.AbstractSignalProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 电流信号处理器
 */
@Slf4j
@Component
public class CurrentProcessor extends AbstractSignalProcessor {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 按照 key：WARN_RULE_KEY_PREFIX:ruleCode:battery_type_id 存储
    private static final String WARN_RULE_KEY_PREFIX = "warnrule";
    
    public CurrentProcessor(warnRuleMapper warnRuleMapper, warnMapper warnMapper) {
        super(warnRuleMapper, warnMapper);
    }

    @Override
    protected String getMaxKey() {
        return "Ix";
    }

    @Override
    protected String getMinKey() {
        return "Ii";
    }

    @Override
    protected String getLogPrefix() {
        return "电流";
    }

    @Override
    protected WarnRule findWarnRule(int batteryTypeId, BigDecimal currentDiff) {

        String warnRuleKey = WARN_RULE_KEY_PREFIX + ":1:" + batteryTypeId;

        try {
            // 用StringRedisTemplate读取原始字符串
            List<String> ruleJsonList = stringRedisTemplate.opsForList().range(warnRuleKey, 0, -1);
            if (ruleJsonList != null && !ruleJsonList.isEmpty()) {
                log.info("从Redis中获取到电压告警规则列表: key={}, size={}", warnRuleKey, ruleJsonList.size());
                for (String json : ruleJsonList) {
                    try {
                        WarnRule rule = null;
                        JsonNode node = objectMapper.readTree(json);
                        if (node.isArray() && node.size() == 2) {
                            // 兼容带@class类型头的json
                            JsonNode realRuleNode = node.get(1);
                            // 手动解析BigDecimal字段
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
                            // 普通对象json
                            rule = objectMapper.readValue(json, WarnRule.class);
                        }
                        if (rule != null && !rule.isDeleted() &&
                                currentDiff.compareTo(rule.getMinThreshold()) >= 0 &&
                                currentDiff.compareTo(rule.getMaxThreshold()) <= 0) {
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

        // 如果Redis中没有找到或发生异常，从数据库查询

        WarnRule rule = warnRuleMapper.getCurrentWarnRule(batteryTypeId, currentDiff);
        if (rule == null) {
            log.info("电流差值{}A小于最小报警阈值，不需要报警", currentDiff);
            rule = new WarnRule();
            rule.setWarnLevel((byte) -1);
        }
        return rule;
    }
} 