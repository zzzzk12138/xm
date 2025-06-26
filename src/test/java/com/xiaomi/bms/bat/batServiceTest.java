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

    @Test
    void createBatteryType_Success() {
        // 准备测试数据
        BatteryType newType = new BatteryType();
        newType.setTypeName("三元电池");
        when(batMapper.insert(any(BatteryType.class))).thenReturn(1);

        // 执行测试
        BatteryType result = batService.createBatteryType(newType);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isDeleted());
        assertEquals("三元电池", result.getTypeName());
        verify(batMapper).insert(any(BatteryType.class));
    }

    @Test
    void getBatteryTypeById_Success() {
        // 准备测试数据
        when(batMapper.selectById(testBatteryType.getId())).thenReturn(testBatteryType);

        // 执行测试
        BatteryType result = batService.getBatteryTypeById(testBatteryType.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(testBatteryType.getId(), result.getId());
        assertEquals(testBatteryType.getTypeName(), result.getTypeName());
        assertFalse(result.isDeleted());
        verify(batMapper).selectById(testBatteryType.getId());
    }

    @Test
    void getBatteryTypeById_NotFound() {
        // 准备测试数据
        when(batMapper.selectById(testBatteryType.getId())).thenReturn(null);

        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> batService.getBatteryTypeById(testBatteryType.getId()));
        assertEquals("未找到ID为: " + testBatteryType.getId() + " 的电池类型", exception.getMessage());
        verify(batMapper).selectById(testBatteryType.getId());
    }

    @Test
    void getBatteryTypeById_Deleted() {
        // 准备测试数据
        testBatteryType.setDeleted(true);
        when(batMapper.selectById(testBatteryType.getId())).thenReturn(testBatteryType);

        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> batService.getBatteryTypeById(testBatteryType.getId()));
        assertEquals("未找到ID为: " + testBatteryType.getId() + " 的电池类型", exception.getMessage());
        verify(batMapper).selectById(testBatteryType.getId());
    }

    @Test
    void getAllBatteryTypes_Success() {
        // 准备测试数据
        BatteryType type2 = new BatteryType();
        type2.setId((byte)2);
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
        verify(batMapper).selectAll();
    }

    @Test
    void updateBatteryType_Success() {
        // 准备测试数据
        BatteryType updateType = new BatteryType();
        updateType.setId(testBatteryType.getId());
        updateType.setTypeName("铁锂电池");

        when(batMapper.existsAndNotDeleted(testBatteryType.getId())).thenReturn(true);
        when(batMapper.selectByTypeName("铁锂电池")).thenReturn(null);
        when(batMapper.selectById(testBatteryType.getId())).thenReturn(testBatteryType);
        when(batMapper.update(any(BatteryType.class))).thenReturn(1);

        // 执行测试
        BatteryType result = batService.updateBatteryType(updateType);

        // 验证结果
        assertNotNull(result);
        assertEquals("铁锂电池", result.getTypeName());
        assertFalse(result.isDeleted());
        verify(batMapper).update(any(BatteryType.class));
    }

    @Test
    void updateBatteryType_NotFound() {
        // 准备测试数据
        when(batMapper.existsAndNotDeleted(testBatteryType.getId())).thenReturn(false);

        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> batService.updateBatteryType(testBatteryType));
        assertEquals("未找到ID为: " + testBatteryType.getId() + " 的电池类型", exception.getMessage());
        verify(batMapper, never()).update(any(BatteryType.class));
    }

    @Test
    void updateBatteryType_DuplicateName() {
        // 准备测试数据
        BatteryType existingType = new BatteryType();
        existingType.setId((byte)2);
        existingType.setTypeName("铁锂电池");
        existingType.setDeleted(false);

        testBatteryType.setTypeName("铁锂电池");

        when(batMapper.existsAndNotDeleted(testBatteryType.getId())).thenReturn(true);
        when(batMapper.selectByTypeName("铁锂电池")).thenReturn(existingType);

        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> batService.updateBatteryType(testBatteryType));
        assertEquals("电池类型名称已存在: 铁锂电池", exception.getMessage());
        verify(batMapper, never()).update(any(BatteryType.class));
    }

    @Test
    void deleteBatteryType_Success() {
        // 准备测试数据
        when(batMapper.existsAndNotDeleted(testBatteryType.getId())).thenReturn(true);
        when(batMapper.softDelete(testBatteryType.getId())).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> batService.deleteBatteryType(testBatteryType.getId()));

        // 验证结果
        verify(batMapper).softDelete(testBatteryType.getId());
    }

    @Test
    void deleteBatteryType_NotFound() {
        // 准备测试数据
        when(batMapper.existsAndNotDeleted(testBatteryType.getId())).thenReturn(false);

        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> batService.deleteBatteryType(testBatteryType.getId()));
        assertEquals("未找到ID为: " + testBatteryType.getId() + " 的电池类型", exception.getMessage());
        verify(batMapper, never()).softDelete(any());
    }

    @Test
    void deleteBatteryType_Failed() {
        // 准备测试数据
        when(batMapper.existsAndNotDeleted(testBatteryType.getId())).thenReturn(true);
        when(batMapper.softDelete(testBatteryType.getId())).thenReturn(0);

        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> batService.deleteBatteryType(testBatteryType.getId()));
        assertEquals("删除电池类型失败", exception.getMessage());
    }

    @Test
    void existsActiveBatteryType_True() {
        // 准备测试数据
        when(batMapper.existsAndNotDeleted(testBatteryType.getId())).thenReturn(true);

        // 执行测试
        boolean result = batService.existsActiveBatteryType(testBatteryType.getId());

        // 验证结果
        assertTrue(result);
        verify(batMapper).existsAndNotDeleted(testBatteryType.getId());
    }

    @Test
    void existsActiveBatteryType_False() {
        // 准备测试数据
        when(batMapper.existsAndNotDeleted(testBatteryType.getId())).thenReturn(false);

        // 执行测试
        boolean result = batService.existsActiveBatteryType(testBatteryType.getId());

        // 验证结果
        assertFalse(result);
        verify(batMapper).existsAndNotDeleted(testBatteryType.getId());
    }
}
