package com.xiaomi.bms.rule;

import com.xiaomi.bms.entity.WarnRule;
import com.xiaomi.bms.mapper.ruleMapper;
import com.xiaomi.bms.service.impl.ruleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ruleServiceTest {
    
    @Mock
    private ruleMapper ruleMapper;
    
    @InjectMocks
    private ruleServiceImpl ruleService;
    
    private WarnRule testRule;
    private static final int TERNARY_BATTERY_TYPE_ID = 1; // 三元电池类型ID
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 初始化测试数据
        testRule = new WarnRule();
        testRule.setRuleId(1);
        testRule.setRuleCode(1001);
        testRule.setRuleName("测试规则");
        testRule.setBatteryTypeId(TERNARY_BATTERY_TYPE_ID);
        testRule.setMinThreshold(new BigDecimal("10.0"));
        testRule.setMaxThreshold(new BigDecimal("90.0"));
        testRule.setWarnLevel((byte) 1);
        testRule.setDeleted(false);
    }
    
    @Test
    void createRule_Success() {
        // 准备测试数据
        when(ruleMapper.insert(any(WarnRule.class))).thenReturn(1);
        
        // 执行测试
        boolean result = ruleService.createRule(testRule);
        
        // 验证结果
        assertTrue(result);
        verify(ruleMapper).insert(testRule);
        assertFalse(testRule.isDeleted());
    }
    
    @Test
    void createRule_Failure() {
        // 准备测试数据
        when(ruleMapper.insert(any(WarnRule.class))).thenReturn(0);
        
        // 执行测试
        boolean result = ruleService.createRule(testRule);
        
        // 验证结果
        assertFalse(result);
        verify(ruleMapper).insert(testRule);
    }
    
    @Test
    void updateRule_Success() {
        // 准备测试数据
        when(ruleMapper.existsById(testRule.getRuleId())).thenReturn(true);
        when(ruleMapper.update(any(WarnRule.class))).thenReturn(1);
        
        // 执行测试
        boolean result = ruleService.updateRule(testRule);
        
        // 验证结果
        assertTrue(result);
        verify(ruleMapper).update(testRule);
    }
    
    @Test
    void updateRule_NotExists() {
        // 准备测试数据
        when(ruleMapper.existsById(testRule.getRuleId())).thenReturn(false);
        
        // 执行测试
        boolean result = ruleService.updateRule(testRule);
        
        // 验证结果
        assertFalse(result);
        verify(ruleMapper, never()).update(any());
    }
    
    @Test
    void getRuleById_Success() {
        // 准备测试数据
        when(ruleMapper.findById(testRule.getRuleId())).thenReturn(testRule);
        
        // 执行测试
        WarnRule result = ruleService.getRuleById(testRule.getRuleId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testRule.getRuleId(), result.getRuleId());
        assertEquals(testRule.getRuleName(), result.getRuleName());
    }
    
    @Test
    void getRuleById_NotFound() {
        // 准备测试数据
        when(ruleMapper.findById(testRule.getRuleId())).thenReturn(null);
        
        // 执行测试
        WarnRule result = ruleService.getRuleById(testRule.getRuleId());
        
        // 验证结果
        assertNull(result);
    }
    
    @Test
    void getAllRules_Success() {
        // 准备测试数据
        List<WarnRule> rules = Arrays.asList(testRule);
        when(ruleMapper.findAll()).thenReturn(rules);
        
        // 执行测试
        List<WarnRule> result = ruleService.getAllRules();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRule.getRuleId(), result.get(0).getRuleId());
    }
    
    @Test
    void getRulesByBatteryTypeId_Success() {
        // 准备测试数据
        List<WarnRule> rules = Arrays.asList(testRule);
        when(ruleMapper.findByBatteryTypeId(testRule.getBatteryTypeId())).thenReturn(rules);
        
        // 执行测试
        List<WarnRule> result = ruleService.getRulesByBatteryTypeId(testRule.getBatteryTypeId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRule.getBatteryTypeId(), result.get(0).getBatteryTypeId());
    }
    
    @Test
    void deleteRule_Success() {
        // 准备测试数据
        when(ruleMapper.existsById(testRule.getRuleId())).thenReturn(true);
        when(ruleMapper.softDelete(testRule.getRuleId())).thenReturn(1);
        
        // 执行测试
        boolean result = ruleService.deleteRule(testRule.getRuleId());
        
        // 验证结果
        assertTrue(result);
        verify(ruleMapper).softDelete(testRule.getRuleId());
    }
    
    @Test
    void deleteRule_NotExists() {
        // 准备测试数据
        when(ruleMapper.existsById(testRule.getRuleId())).thenReturn(false);
        
        // 执行测试
        boolean result = ruleService.deleteRule(testRule.getRuleId());
        
        // 验证结果
        assertFalse(result);
        verify(ruleMapper, never()).softDelete(any());
    }

    @Test
    void initializeTernaryBatteryVoltageRules_Success() {
        // 准备测试数据 - 五个电压差报警规则
        WarnRule[] rules = {
            createVoltageRule(5.0, Double.MAX_VALUE, (byte) 0, "电压差超过5V"),
            createVoltageRule(3.0, 5.0, (byte) 1, "电压差在3-5V之间"),
            createVoltageRule(1.0, 3.0, (byte) 2, "电压差在1-3V之间"),
            createVoltageRule(0.6, 1.0, (byte) 3, "电压差在0.6-1V之间"),
            createVoltageRule(0.2, 0.6, (byte) 4, "电压差在0.2-0.6V之间")
        };

        // 模拟每个规则的插入操作
        for (WarnRule rule : rules) {
            when(ruleMapper.insert(any(WarnRule.class))).thenReturn(1);
        }

        // 执行测试并验证每个规则
        for (WarnRule rule : rules) {
            boolean result = ruleService.createRule(rule);
            assertTrue(result);
            verify(ruleMapper).insert(rule);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "5.1, 0",    // 电压差 >= 5V, 报警等级 0
        "4.0, 1",    // 3V <= 电压差 < 5V, 报警等级 1
        "2.0, 2",    // 1V <= 电压差 < 3V, 报警等级 2
        "0.8, 3",    // 0.6V <= 电压差 < 1V, 报警等级 3
        "0.4, 4",    // 0.2V <= 电压差 < 0.6V, 报警等级 4
        "0.1, -1"    // 电压差 < 0.2V, 不报警
    })
    void testVoltageRules(double voltageDiff, int expectedWarnLevel) {
        // 准备测试数据
        List<WarnRule> rules = Arrays.asList(
            createVoltageRule(5.0, Double.MAX_VALUE, (byte) 0, "电压差超过5V"),
            createVoltageRule(3.0, 5.0, (byte) 1, "电压差在3-5V之间"),
            createVoltageRule(1.0, 3.0, (byte) 2, "电压差在1-3V之间"),
            createVoltageRule(0.6, 1.0, (byte) 3, "电压差在0.6-1V之间"),
            createVoltageRule(0.2, 0.6, (byte) 4, "电压差在0.2-0.6V之间")
        );

        when(ruleMapper.findByBatteryTypeId(TERNARY_BATTERY_TYPE_ID)).thenReturn(rules);

        // 执行测试
        List<WarnRule> result = ruleService.getRulesByBatteryTypeId(TERNARY_BATTERY_TYPE_ID);
        
        // 验证结果
        assertNotNull(result);
        byte actualWarnLevel = findWarnLevelForVoltageDiff(result, voltageDiff);
        assertEquals(expectedWarnLevel, actualWarnLevel);
    }

    private WarnRule createVoltageRule(double minThreshold, double maxThreshold, byte warnLevel, String ruleName) {
        WarnRule rule = new WarnRule();
        rule.setBatteryTypeId(TERNARY_BATTERY_TYPE_ID);
        rule.setRuleCode(1000 + warnLevel); // 使用报警等级作为规则代码的一部分
        rule.setRuleName(ruleName);
        rule.setMinThreshold(BigDecimal.valueOf(minThreshold));
        rule.setMaxThreshold(BigDecimal.valueOf(maxThreshold));
        rule.setWarnLevel(warnLevel);
        rule.setDeleted(false);
        return rule;
    }

    private byte findWarnLevelForVoltageDiff(List<WarnRule> rules, double voltageDiff) {
        for (WarnRule rule : rules) {
            if (voltageDiff >= rule.getMinThreshold().doubleValue() && 
                voltageDiff < rule.getMaxThreshold().doubleValue()) {
                return rule.getWarnLevel();
            }
        }
        return -1; // 不报警
    }
}
