package com.xiaomi.warn.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaomi.warn.dto.warnDTO;
import com.xiaomi.warn.entity.BatteryType;
import com.xiaomi.warn.entity.Vehicle;
import com.xiaomi.warn.entity.Warn;
import com.xiaomi.warn.mapper.vehicleMapper;
import com.xiaomi.warn.mapper.warnMapper;
import com.xiaomi.warn.processor.impl.CurrentProcessor;
import com.xiaomi.warn.processor.impl.VoltageProcessor;
import com.xiaomi.warn.service.Impl.warnServiceImpl;
import com.xiaomi.warn.service.vehicleService;
import com.xiaomi.warn.task.warnSignalProviderTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class warnServiceTest {
    
    @Mock
    private warnMapper warnMapper;
    
    @Mock
    private vehicleService vehicleService;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private vehicleMapper vehicleMapper;
    
    @Mock
    private warnSignalProviderTask warnSignalProviderTask;
    
    @Mock
    private VoltageProcessor voltageProcessor;
    
    @Mock
    private CurrentProcessor currentProcessor;
    
    @Mock
    private ExecutorService executorService;
    
    @InjectMocks
    private warnServiceImpl warnService;
    
    private warnDTO testWarnDTO;
    private static final Integer TEST_CAR_ID = 100;
    private static final String TEST_SIGNAL = "{\"Mx\":4.2,\"Mi\":3.0,\"Ix\":10.0,\"Ii\":-10.0}";
    
    @BeforeEach
    void setUp() {
        // 初始化mock
        MockitoAnnotations.openMocks(this);
        
        // 初始化测试用的警告数据
        testWarnDTO = new warnDTO();
        testWarnDTO.setCarId(TEST_CAR_ID);
        testWarnDTO.setSignal(TEST_SIGNAL);
        
        // 模拟BatteryType
        BatteryType batteryType = new BatteryType();
        batteryType.setId(1);
        batteryType.setTypeName("锂电池");
        when(vehicleService.getBatteryTypeByCarId(TEST_CAR_ID)).thenReturn(batteryType);
        
        // 模拟Vehicle
        Vehicle vehicle = new Vehicle();
        vehicle.setCarId(TEST_CAR_ID);
        when(vehicleService.getVehicleByCarId(TEST_CAR_ID)).thenReturn(vehicle);
        
        // 模拟ObjectMapper
        Map<String, Double> signalMap = new HashMap<>();
        signalMap.put("Mx", 4.2);
        signalMap.put("Mi", 3.0);
        signalMap.put("Ix", 10.0);
        signalMap.put("Ii", -10.0);
        try {
            when(objectMapper.readValue(eq(TEST_SIGNAL), any(Class.class))).thenReturn(signalMap);
        } catch (Exception e) {
            fail("Mock objectMapper failed");
        }
    }
    
    @Test
    void processWarns_EmptyList() throws JsonProcessingException {
        // 执行测试并验证异常
        List<Map<String, Object>> results = warnService.processWarns(Collections.emptyList());
        
        // 验证结果
        assertTrue(results.isEmpty());
        
        // 验证调用
        verify(vehicleService, never()).getBatteryTypeByCarId(any());
        verify(objectMapper, never()).readValue(anyString(), any(Class.class));
    }
    
    @Test
    void processWarns_NullList() {
        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> warnService.processWarns(null));
        assertEquals("警告信息列表不能为null", exception.getMessage());
    }
    
    @Test
    void getWarnsByCarId_Success() {
        // 准备测试数据
        Warn warn = new Warn();
        warn.setCarId(TEST_CAR_ID);
        warn.setBatTypename("锂电池");
        warn.setWarnName("电压异常");
        warn.setWarnLevel(1);
        warn.setCreatedAt(LocalDateTime.now());
        List<Warn> warns = Collections.singletonList(warn);
        
        // 模拟mapper行为
        when(warnMapper.findWarnsByCarId(TEST_CAR_ID)).thenReturn(warns);
        
        // 执行测试
        List<Map<String, Object>> results = warnService.getWarnsByCarId(TEST_CAR_ID);
        
        // 验证结果
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(TEST_CAR_ID, results.get(0).get("车架编号"));
        assertEquals("锂电池", results.get(0).get("电池类型"));
        assertEquals("电压异常", results.get(0).get("预警名称"));
        assertEquals(1, results.get(0).get("预警等级"));
        
        // 验证调用
        verify(vehicleService).getVehicleByCarId(TEST_CAR_ID);
        verify(warnMapper).findWarnsByCarId(TEST_CAR_ID);
    }
    
    @Test
    void getWarnsByCarId_VehicleNotFound() {
        // 模拟车辆不存在
        when(vehicleService.getVehicleByCarId(TEST_CAR_ID)).thenReturn(null);
        
        // 执行测试
        List<Map<String, Object>> results = warnService.getWarnsByCarId(TEST_CAR_ID);
        
        // 验证结果
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(TEST_CAR_ID, results.get(0).get("车架编号"));
        assertEquals("未找到车辆信息", results.get(0).get("error"));
        
        // 验证调用
        verify(vehicleService).getVehicleByCarId(TEST_CAR_ID);
        verify(warnMapper, never()).findWarnsByCarId(anyInt());
    }
    
    @Test
    void getWarnsByCarId_InvalidId() {
        // 执行测试
        List<Map<String, Object>> results = warnService.getWarnsByCarId(null);
        
        // 验证结果
        assertNotNull(results);
        assertEquals(1, results.size());
        Map<String, Object> result = results.get(0);
        assertNull(result.get("车架编号"));
        assertEquals("未找到车辆信息", result.get("error"));
        
        // 验证没有进行数据库查询
        verify(warnMapper, never()).findWarnsByCarId(anyInt());
        verifyNoInteractions(vehicleService);
    }
}
