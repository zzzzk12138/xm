package com.xiaomi.bms.bat;

import com.xiaomi.bms.entity.BatteryType;
import com.xiaomi.bms.mapper.batMapper;
import com.xiaomi.bms.service.impl.batServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class batServiceTest {

    @Mock
    private batMapper batMapper;

    @InjectMocks
    private batServiceImpl batService;

    private BatteryType testBatteryType;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 初始化测试用电池类型数据
        testBatteryType = new BatteryType();
        testBatteryType.setId(1);
        testBatteryType.setTypeName("三元电池");
        testBatteryType.setDeleted(false);
    }

    // 测试创建电池类型相关方法
    @Test
    void createBatteryType_Success() {
        // 准备测试数据
        BatteryType newType = new BatteryType();
        newType.setTypeName("三元电池");
        
        // 模拟mapper行为
        when(batMapper.insert(any(BatteryType.class))).thenAnswer(invocation -> {
            BatteryType batteryType = invocation.getArgument(0);
            batteryType.setId(1);
            return 1;
        });
        
        // 执行测试
        BatteryType result = batService.createBatteryType(newType);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("三元电池", result.getTypeName());
        assertFalse(result.isDeleted());
        
        // 验证调用
        verify(batMapper).insert(any(BatteryType.class));
    }

    // 测试根据ID查询电池类型相关方法
    @Test
    void getBatteryTypeById_Success() {
        // 模拟mapper行为
        when(batMapper.selectById(1)).thenReturn(testBatteryType);
        
        // 执行测试
        BatteryType result = batService.getBatteryTypeById(1);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("三元电池", result.getTypeName());
        assertFalse(result.isDeleted());
        
        // 验证调用
        verify(batMapper).selectById(1);
    }

    @Test
    void getBatteryTypeById_NotFound() {
        // 模拟mapper行为
        when(batMapper.selectById(1)).thenReturn(null);
        
        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> batService.getBatteryTypeById(1));
        assertEquals("未找到ID为: 1 的电池类型", exception.getMessage());
        
        // 验证调用
        verify(batMapper).selectById(1);
    }

    @Test
    void getBatteryTypeById_Deleted() {
        // 准备已删除的测试数据
        testBatteryType.setDeleted(true);
        when(batMapper.selectById(1)).thenReturn(testBatteryType);
        
        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> batService.getBatteryTypeById(1));
        assertEquals("未找到ID为: 1 的电池类型", exception.getMessage());
        
        // 验证调用
        verify(batMapper).selectById(1);
    }

    // 测试获取所有电池类型相关方法
    @Test
    void getAllBatteryTypes_Success() {
        // 准备测试数据
        BatteryType type2 = new BatteryType();
        type2.setId(2);
        type2.setTypeName("铁锂电池");
        type2.setDeleted(false);
        
        List<BatteryType> expectedTypes = Arrays.asList(testBatteryType, type2);
        when(batMapper.selectAll()).thenReturn(expectedTypes);
        
        // 执行测试
        List<BatteryType> result = batService.getAllBatteryTypes();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("三元电池", result.get(0).getTypeName());
        assertEquals("铁锂电池", result.get(1).getTypeName());
        
        // 验证调用
        verify(batMapper).selectAll();
    }

    @Test
    void getAllBatteryTypes_Empty() {
        // 模拟空列表
        when(batMapper.selectAll()).thenReturn(Arrays.asList());
        
        // 执行测试
        List<BatteryType> result = batService.getAllBatteryTypes();
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // 验证调用
        verify(batMapper).selectAll();
    }

    // 测试更新电池类型相关方法
    @Test
    void updateBatteryType_Success() {
        // 准备测试数据
        BatteryType updateType = new BatteryType();
        updateType.setId(1);
        updateType.setTypeName("铁锂电池");
        
        // 模拟mapper行为
        when(batMapper.existsAndNotDeleted(1)).thenReturn(true);
        when(batMapper.selectByTypeName("铁锂电池")).thenReturn(null);
        when(batMapper.selectById(1)).thenReturn(testBatteryType);
        when(batMapper.update(any(BatteryType.class))).thenReturn(1);
        
        // 执行测试
        BatteryType result = batService.updateBatteryType(updateType);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("铁锂电池", result.getTypeName());
        assertFalse(result.isDeleted());
        
        // 验证调用
        verify(batMapper).existsAndNotDeleted(1);
        verify(batMapper).selectByTypeName("铁锂电池");
        verify(batMapper).selectById(1);
        verify(batMapper).update(any(BatteryType.class));
    }

    @Test
    void updateBatteryType_NotFound() {
        // 准备测试数据
        BatteryType updateType = new BatteryType();
        updateType.setId(1);
        updateType.setTypeName("铁锂电池");
        
        // 模拟mapper行为
        when(batMapper.existsAndNotDeleted(1)).thenReturn(false);
        
        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> batService.updateBatteryType(updateType));
        assertEquals("未找到ID为: 1 的电池类型", exception.getMessage());
        
        // 验证调用
        verify(batMapper).existsAndNotDeleted(1);
        verify(batMapper, never()).update(any(BatteryType.class));
    }

    @Test
    void updateBatteryType_DuplicateName() {
        // 准备测试数据
        BatteryType updateType = new BatteryType();
        updateType.setId(1);
        updateType.setTypeName("铁锂电池");
        
        BatteryType existingType = new BatteryType();
        existingType.setId(2);
        existingType.setTypeName("铁锂电池");
        
        // 模拟mapper行为
        when(batMapper.existsAndNotDeleted(1)).thenReturn(true);
        when(batMapper.selectByTypeName("铁锂电池")).thenReturn(existingType);
        
        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> batService.updateBatteryType(updateType));
        assertEquals("电池类型名称已存在: 铁锂电池", exception.getMessage());
        
        // 验证调用
        verify(batMapper).existsAndNotDeleted(1);
        verify(batMapper).selectByTypeName("铁锂电池");
        verify(batMapper, never()).update(any(BatteryType.class));
    }

    // 测试删除电池类型相关方法
    @Test
    void deleteBatteryType_Success() {
        // 模拟mapper行为
        when(batMapper.existsAndNotDeleted(1)).thenReturn(true);
        when(batMapper.softDelete(1)).thenReturn(1);
        
        // 执行测试
        assertDoesNotThrow(() -> batService.deleteBatteryType(1));
        
        // 验证调用
        verify(batMapper).existsAndNotDeleted(1);
        verify(batMapper).softDelete(1);
    }

    @Test
    void deleteBatteryType_NotFound() {
        // 模拟mapper行为
        when(batMapper.existsAndNotDeleted(1)).thenReturn(false);
        
        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> batService.deleteBatteryType(1));
        assertEquals("未找到ID为: 1 的电池类型", exception.getMessage());
        
        // 验证调用
        verify(batMapper).existsAndNotDeleted(1);
        verify(batMapper, never()).softDelete(1);
    }

    @Test
    void deleteBatteryType_Failed() {
        // 模拟mapper行为
        when(batMapper.existsAndNotDeleted(1)).thenReturn(true);
        when(batMapper.softDelete(1)).thenReturn(0);
        
        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> batService.deleteBatteryType(1));
        assertEquals("删除电池类型失败", exception.getMessage());
        
        // 验证调用
        verify(batMapper).existsAndNotDeleted(1);
        verify(batMapper).softDelete(1);
    }

    // 测试检查电池类型是否存在相关方法
    @Test
    void existsActiveBatteryType_True() {
        // 模拟mapper行为
        when(batMapper.existsAndNotDeleted(1)).thenReturn(true);
        
        // 执行测试
        boolean result = batService.existsActiveBatteryType(1);
        
        // 验证结果
        assertTrue(result);
        
        // 验证调用
        verify(batMapper).existsAndNotDeleted(1);
    }

    @Test
    void existsActiveBatteryType_False() {
        // 模拟mapper行为
        when(batMapper.existsAndNotDeleted(1)).thenReturn(false);
        
        // 执行测试
        boolean result = batService.existsActiveBatteryType(1);
        
        // 验证结果
        assertFalse(result);
        
        // 验证调用
        verify(batMapper).existsAndNotDeleted(1);
    }
}
