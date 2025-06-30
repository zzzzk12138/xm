package com.xiaomi.signal.test;

import com.xiaomi.signal.entity.Signal;
import com.xiaomi.signal.mapper.signalMapper;
import com.xiaomi.signal.service.impl.signalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class signalServiceTest {
    
    @Mock
    private signalMapper signalMapper;
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    private RedisLockRegistry redisLockRegistry;
    
    @Mock
    private ValueOperations<String, Object> valueOperations;
    
    @Mock
    private Lock lock;
    
    @InjectMocks
    private signalServiceImpl signalService;
    
    private Signal testSignal;
    private static final String TEST_VID = "TEST_VID_001";
    private static final Long TEST_SIGNAL_ID = 1L;
    
    @BeforeEach
    void setUp() throws Exception {
        // 使用静态方法初始化mocks
        MockitoAnnotations.openMocks(this);
        
        // 手动创建和配置RedisLockRegistry mock
        redisLockRegistry = mock(RedisLockRegistry.class);
        
        // 手动注入redisLockRegistry
        ReflectionTestUtils.setField(signalService, "redisLockRegistry", redisLockRegistry);
        
        // 初始化测试用信号数据
        testSignal = new Signal();
        testSignal.setSignalId(TEST_SIGNAL_ID);
        testSignal.setVid(TEST_VID);
        testSignal.setMaxVoltage(new BigDecimal("4.2"));
        testSignal.setMinVoltage(new BigDecimal("3.0"));
        testSignal.setMaxCurrent(new BigDecimal("10.0"));
        testSignal.setMinCurrent(new BigDecimal("-10.0"));
        testSignal.setRecordedAt(LocalDateTime.now());
        testSignal.setStatus(0);
        
        // Mock RedisTemplate 相关行为
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisLockRegistry.obtain(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(true);
    }
    
    @Test
    void createSignal_Success() {
        // 准备测试数据
        Signal newSignal = new Signal();
        newSignal.setVid(TEST_VID);
        newSignal.setMaxVoltage(new BigDecimal("4.2"));
        newSignal.setMinVoltage(new BigDecimal("3.0"));
        newSignal.setMaxCurrent(new BigDecimal("10.0"));
        newSignal.setMinCurrent(new BigDecimal("-10.0"));
        
        // 模拟Mapper行为
        when(signalMapper.save(any(Signal.class))).thenAnswer(invocation -> {
            Signal signal = invocation.getArgument(0);
            signal.setSignalId(TEST_SIGNAL_ID);
            return 1;
        });
        
        // 执行测试
        Signal result = signalService.createSignal(newSignal);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_SIGNAL_ID, result.getSignalId());
        assertEquals(TEST_VID, result.getVid());
        assertEquals(0, result.getStatus());
        assertNotNull(result.getRecordedAt());
        
        // 验证调用
        verify(signalMapper).save(any(Signal.class));
        verify(redisTemplate.opsForValue()).set(anyString(), any(Signal.class), anyLong(), any(TimeUnit.class));
        verify(redisTemplate).delete(anyString());
    }
    
    @Test
    void createSignal_NullSignal() {
        // 执行测试并验证异常
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> signalService.createSignal(null));
        assertEquals("信号数据不能为空", exception.getMessage());
        
        // 验证调用
        verify(signalMapper, never()).save(any(Signal.class));
    }
    
    @Test
    void createSignal_EmptyVid() {
        // 准备测试数据
        Signal signal = new Signal();
        
        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            signalService.createSignal(signal);
        });
        
        // 验证异常消息
        assertEquals("车辆VID不能为空", exception.getMessage());
    }
    

    @Test
    void getSignalById_FromCache() {
        // 模拟Redis缓存中有数据
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(testSignal);
        
        // 执行测试
        Signal result = signalService.getSignalById(TEST_SIGNAL_ID);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_SIGNAL_ID, result.getSignalId());
        assertEquals(TEST_VID, result.getVid());
        
        // 验证调用
        verify(redisTemplate.opsForValue()).get(anyString());
        verify(signalMapper, never()).findById(anyLong());
    }

    
    @Test
    void getSignalsByVid_FromCache() {
        // 准备测试数据
        List<Signal> signals = Arrays.asList(testSignal);
        
        // 模拟Redis缓存中有数据
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(signals);
        
        // 执行测试
        List<Signal> result = signalService.getSignalsByVid(TEST_VID);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_VID, result.get(0).getVid());
        
        // 验证调用
        verify(redisTemplate.opsForValue()).get(anyString());
        verify(signalMapper, never()).findByVid(anyString());
    }
    
    @Test
    void getSignalsByVid_EmptyVid() {
        // 准备测试数据
        String emptyVid = "";
        
        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            signalService.deleteSignalsByVid(emptyVid);
        });
        
        // 验证异常消息
        assertEquals("车辆VID不能为空", exception.getMessage());
    }
    
    @Test
    void getAllSignals_Success() {
        // 准备测试数据
        List<Signal> signals = Arrays.asList(testSignal);
        
        // 模拟mapper行为
        when(signalMapper.getAllSignals()).thenReturn(signals);
        
        // 执行测试
        List<Signal> result = signalService.getAllSignals();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_VID, result.get(0).getVid());
        
        // 验证调用
        verify(signalMapper).getAllSignals();
    }
    
    @Test
    void updateSignal_Success() {
        // 准备测试数据
        Signal updateSignal = new Signal();
        updateSignal.setSignalId(TEST_SIGNAL_ID);
        updateSignal.setMaxVoltage(new BigDecimal("4.5"));
        updateSignal.setMinVoltage(new BigDecimal("3.2"));
        
        // 模拟mapper行为
        when(signalMapper.findById(TEST_SIGNAL_ID)).thenReturn(testSignal);
        when(signalMapper.update(any(Signal.class))).thenReturn(1);
        
        // 执行测试
        Signal result = signalService.updateSignal(updateSignal);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_SIGNAL_ID, result.getSignalId());
        assertEquals(new BigDecimal("4.5"), result.getMaxVoltage());
        assertEquals(new BigDecimal("3.2"), result.getMinVoltage());
        
        // 验证调用
        verify(signalMapper).findById(TEST_SIGNAL_ID);
        verify(signalMapper).update(any(Signal.class));
        verify(redisTemplate).delete(anyString());
    }
    
    @Test
    void updateSignal_NotFound() {
        // 准备测试数据
        Signal updateSignal = new Signal();
        updateSignal.setSignalId(TEST_SIGNAL_ID);
        
        // 模拟mapper行为
        when(signalMapper.findById(TEST_SIGNAL_ID)).thenReturn(null);
        
        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class,
            () -> signalService.updateSignal(updateSignal));
        assertEquals("未找到ID为: " + TEST_SIGNAL_ID + " 的信号数据", exception.getMessage());
        
        // 验证调用
        verify(signalMapper).findById(TEST_SIGNAL_ID);
        verify(signalMapper, never()).update(any(Signal.class));
    }
    
    @Test
    void deleteSignal_Success() {
        // 模拟mapper行为
        when(signalMapper.existsById(TEST_SIGNAL_ID)).thenReturn(true);
        when(signalMapper.softDelete(TEST_SIGNAL_ID)).thenReturn(1);
        
        // 执行测试
        assertDoesNotThrow(() -> signalService.deleteSignal(TEST_SIGNAL_ID));
        
        // 验证调用
        verify(signalMapper).existsById(TEST_SIGNAL_ID);
        verify(signalMapper).softDelete(TEST_SIGNAL_ID);
        verify(redisTemplate).delete(anyString());
    }
    
    @Test
    void deleteSignalsByVid_Success() {
        // 准备测试数据
        List<Signal> signals = Arrays.asList(testSignal);
        
        // 模拟mapper行为
        when(signalMapper.findByVid(TEST_VID)).thenReturn(signals);
        when(signalMapper.softDeleteByVid(TEST_VID)).thenReturn(1);
        
        // 执行测试
        assertDoesNotThrow(() -> signalService.deleteSignalsByVid(TEST_VID));
        
        // 验证调用
        verify(signalMapper).findByVid(TEST_VID);
        verify(signalMapper).softDeleteByVid(TEST_VID);
        verify(redisTemplate, times(2)).delete(anyString()); // 删除信号ID和VID的缓存
    }
    
    @Test
    void existsSignal_ExistsInCache() {
        // 模拟Redis缓存中存在key
        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        
        // 执行测试
        boolean result = signalService.existsSignal(TEST_SIGNAL_ID);
        
        // 验证结果
        assertTrue(result);
        
        // 验证调用
        verify(redisTemplate).hasKey(anyString());
        verify(signalMapper, never()).existsById(anyLong());
    }
    
    @Test
    void existsSignal_ExistsInDatabase() {
        // 模拟Redis缓存中不存在key，但数据库中存在
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(signalMapper.existsById(TEST_SIGNAL_ID)).thenReturn(true);
        
        // 执行测试
        boolean result = signalService.existsSignal(TEST_SIGNAL_ID);
        
        // 验证结果
        assertTrue(result);
        
        // 验证调用
        verify(redisTemplate).hasKey(anyString());
        verify(signalMapper).existsById(TEST_SIGNAL_ID);
    }
    
    @Test
    void existsSignal_NotExists() {
        // 模拟Redis缓存和数据库中都不存在
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(signalMapper.existsById(TEST_SIGNAL_ID)).thenReturn(false);
        
        // 执行测试
        boolean result = signalService.existsSignal(TEST_SIGNAL_ID);
        
        // 验证结果
        assertFalse(result);
        
        // 验证调用
        verify(redisTemplate).hasKey(anyString());
        verify(signalMapper).existsById(TEST_SIGNAL_ID);
    }
    
    @Test
    void getSignalById_FromDatabase() {
        // 模拟Redis缓存中没有数据
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null);
        // 模拟从数据库中获取数据
        when(signalMapper.findById(TEST_SIGNAL_ID)).thenReturn(testSignal);
        
        // 执行测试
        Signal result = signalService.getSignalById(TEST_SIGNAL_ID);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_SIGNAL_ID, result.getSignalId());
        assertEquals(TEST_VID, result.getVid());
        
        // 验证调用
        verify(redisTemplate.opsForValue(), times(2)).get(anyString());
        verify(signalMapper).findById(TEST_SIGNAL_ID);
        verify(redisTemplate.opsForValue()).set(anyString(), any(Signal.class), anyLong(), any(TimeUnit.class));
    }
    
    @Test
    void getSignalsByVid_FromDatabase() {
        // 准备测试数据
        List<Signal> signals = Arrays.asList(testSignal);
        
        // 模拟Redis缓存中没有数据
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null);
        // 模拟从数据库中获取数据
        when(signalMapper.findByVid(TEST_VID)).thenReturn(signals);
        
        // 执行测试
        List<Signal> result = signalService.getSignalsByVid(TEST_VID);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_VID, result.get(0).getVid());
        
        // 验证调用
        verify(redisTemplate.opsForValue(), times(2)).get(anyString());
        verify(signalMapper).findByVid(TEST_VID);
        verify(redisTemplate.opsForValue()).set(anyString(), any(List.class), anyLong(), any(TimeUnit.class));
    }
    
    @Test
    void deleteSignal_NotFound() {
        // 模拟数据不存在的情况
        when(signalMapper.existsById(TEST_SIGNAL_ID)).thenReturn(false);
        
        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> signalService.deleteSignal(TEST_SIGNAL_ID));
        assertEquals("删除信号失败", exception.getMessage());
        
        // 验证调用
        verify(signalMapper).existsById(TEST_SIGNAL_ID);
        verify(signalMapper, never()).softDelete(TEST_SIGNAL_ID);
        verify(redisTemplate, never()).delete(anyString());
    }
}
