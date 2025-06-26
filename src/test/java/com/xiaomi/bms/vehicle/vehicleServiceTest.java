package com.xiaomi.bms.vehicle;

import com.xiaomi.bms.entity.Vehicle;
import com.xiaomi.bms.service.impl.vehicleServiceImpl;
import com.xiaomi.bms.mapper.vehicleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class vehicleServiceTest {

    @InjectMocks
    private vehicleServiceImpl vehicleService;

    @Mock
    private vehicleMapper vehicleMapper;

    private Vehicle testVehicle;
    private static final String TEST_VID = "VH24A1B2C3D4E5F6";
    private static final int TEST_CAR_ID = 1001;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 初始化测试用车辆数据
        testVehicle = new Vehicle();
        testVehicle.setVid(TEST_VID);
        testVehicle.setCarId(TEST_CAR_ID);
        testVehicle.setBatteryTypeId(1);
        testVehicle.setTotalMileage(100);
        testVehicle.setBatteryHealth(95);
        testVehicle.setCreatedAt(LocalDateTime.now());
        testVehicle.setUpdatedAt(LocalDateTime.now());
        testVehicle.setDeleted(false);
    }

    @Test
    void createVehicle_Success() {
        // 准备测试数据
        Vehicle newVehicle = new Vehicle();
        newVehicle.setCarId(TEST_CAR_ID);
        newVehicle.setBatteryTypeId(1);

        // 模拟mapper行为
        when(vehicleMapper.existsByCarId(TEST_CAR_ID)).thenReturn(false);
        when(vehicleMapper.save(any(Vehicle.class))).thenReturn(1);

        // 执行测试
        Vehicle result = vehicleService.createVehicle(newVehicle);

        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_CAR_ID, result.getCarId());
        assertEquals(1, result.getBatteryTypeId());
        assertEquals(0, result.getTotalMileage());
        assertEquals(100, result.getBatteryHealth());
        assertFalse(result.isDeleted());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        
        // 验证调用
        verify(vehicleMapper).existsByCarId(TEST_CAR_ID);
        verify(vehicleMapper).save(any(Vehicle.class));
    }

    @Test
    void createVehicle_DuplicateCarId() {
        // 准备测试数据
        Vehicle newVehicle = new Vehicle();
        newVehicle.setCarId(TEST_CAR_ID);
        newVehicle.setBatteryTypeId(1);

        when(vehicleMapper.existsByCarId(TEST_CAR_ID)).thenReturn(true);

        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> vehicleService.createVehicle(newVehicle));
        assertEquals("车架号已存在: " + TEST_CAR_ID, exception.getMessage());
        
        verify(vehicleMapper).existsByCarId(TEST_CAR_ID);
        verify(vehicleMapper, never()).save(any());
    }

    @Test
    void getVehicleByVid_Success() {
        when(vehicleMapper.findById(TEST_VID)).thenReturn(testVehicle);

        Vehicle result = vehicleService.getVehicleByVid(TEST_VID);

        assertNotNull(result);
        assertEquals(TEST_VID, result.getVid());
        assertEquals(TEST_CAR_ID, result.getCarId());
        verify(vehicleMapper).findById(TEST_VID);
    }

    @Test
    void getVehicleByVid_NotFound() {
        when(vehicleMapper.findById(TEST_VID)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> vehicleService.getVehicleByVid(TEST_VID));
        assertEquals("未找到VID为: " + TEST_VID + " 的车辆", exception.getMessage());
        verify(vehicleMapper).findById(TEST_VID);
    }

    @Test
    void getVehicleByCarId_Success() {
        when(vehicleMapper.findByCarId(TEST_CAR_ID)).thenReturn(testVehicle);

        Vehicle result = vehicleService.getVehicleByCarId(TEST_CAR_ID);

        assertNotNull(result);
        assertEquals(TEST_VID, result.getVid());
        assertEquals(TEST_CAR_ID, result.getCarId());
        verify(vehicleMapper).findByCarId(TEST_CAR_ID);
    }

    @Test
    void getVehicleByCarId_NotFound() {
        when(vehicleMapper.findByCarId(TEST_CAR_ID)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> vehicleService.getVehicleByCarId(TEST_CAR_ID));
        assertEquals("未找到车架号为: " + TEST_CAR_ID + " 的车辆", exception.getMessage());
        verify(vehicleMapper).findByCarId(TEST_CAR_ID);
    }

    @Test
    void getAllVehicles_Success() {
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setVid("VH24F1E2D3C4B5A6");
        vehicle2.setCarId(1002);
        
        when(vehicleMapper.findAll()).thenReturn(Arrays.asList(testVehicle, vehicle2));

        List<Vehicle> results = vehicleService.getAllVehicles();

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(TEST_VID, results.get(0).getVid());
        assertEquals("VH24F1E2D3C4B5A6", results.get(1).getVid());
        verify(vehicleMapper).findAll();
    }

    @Test
    void updateVehicle_Success() {
        // 准备测试数据
        Vehicle updateVehicle = new Vehicle();
        updateVehicle.setVid(TEST_VID);
        updateVehicle.setTotalMileage(200);
        updateVehicle.setBatteryHealth(90);

        // 设置原始更新时间为1分钟前
        LocalDateTime originalUpdateTime = LocalDateTime.now().minusMinutes(1);
        testVehicle.setUpdatedAt(originalUpdateTime);
        when(vehicleMapper.findById(TEST_VID)).thenReturn(testVehicle);
        when(vehicleMapper.update(any(Vehicle.class))).thenReturn(1);

        // 执行测试
        Vehicle result = vehicleService.updateVehicle(updateVehicle);

        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_VID, result.getVid());
        assertEquals(200, result.getTotalMileage());
        assertEquals(90, result.getBatteryHealth());
        assertNotNull(result.getUpdatedAt());
        assertTrue(result.getUpdatedAt().isAfter(originalUpdateTime));
        
        verify(vehicleMapper).findById(TEST_VID);
        verify(vehicleMapper).update(any(Vehicle.class));
    }

    @Test
    void updateVehicle_NotFound() {
        Vehicle updateVehicle = new Vehicle();
        updateVehicle.setVid(TEST_VID);
        
        when(vehicleMapper.findById(TEST_VID)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> vehicleService.updateVehicle(updateVehicle));
        assertEquals("未找到VID为: " + TEST_VID + " 的车辆", exception.getMessage());
        
        verify(vehicleMapper).findById(TEST_VID);
        verify(vehicleMapper, never()).update(any());
    }

    @Test
    void deleteVehicle_Success() {
        when(vehicleMapper.existsById(TEST_VID)).thenReturn(true);
        when(vehicleMapper.softDelete(TEST_VID)).thenReturn(1);

        assertDoesNotThrow(() -> vehicleService.deleteVehicle(TEST_VID));

        verify(vehicleMapper).existsById(TEST_VID);
        verify(vehicleMapper).softDelete(TEST_VID);
    }

    @Test
    void deleteVehicle_NotFound() {
        when(vehicleMapper.existsById(TEST_VID)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> vehicleService.deleteVehicle(TEST_VID));
        assertEquals("未找到VID为: " + TEST_VID + " 的车辆", exception.getMessage());
        
        verify(vehicleMapper).existsById(TEST_VID);
        verify(vehicleMapper, never()).softDelete(TEST_VID);
    }

    @Test
    void deleteVehicle_Failed() {
        when(vehicleMapper.existsById(TEST_VID)).thenReturn(true);
        when(vehicleMapper.softDelete(TEST_VID)).thenReturn(0);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> vehicleService.deleteVehicle(TEST_VID));
        assertEquals("删除车辆失败", exception.getMessage());
        
        verify(vehicleMapper).existsById(TEST_VID);
        verify(vehicleMapper).softDelete(TEST_VID);
    }

    @Test
    void updateVehicleByCarId_Success() {
        // 准备测试数据
        Vehicle updateVehicle = new Vehicle();
        updateVehicle.setTotalMileage(200);
        updateVehicle.setBatteryHealth(90);

        LocalDateTime beforeUpdate = LocalDateTime.now();
        when(vehicleMapper.findByCarId(TEST_CAR_ID)).thenReturn(testVehicle);
        when(vehicleMapper.update(any(Vehicle.class))).thenReturn(1);

        // 执行测试
        Vehicle result = vehicleService.updateVehicleByCarId(TEST_CAR_ID, updateVehicle);

        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_VID, result.getVid());
        assertEquals(200, result.getTotalMileage());
        assertEquals(90, result.getBatteryHealth());
        assertTrue(result.getUpdatedAt().isAfter(beforeUpdate));
        
        verify(vehicleMapper).findByCarId(TEST_CAR_ID);
        verify(vehicleMapper).update(any(Vehicle.class));
    }

    @Test
    void deleteVehicleByCarId_Success() {
        when(vehicleMapper.existsByCarId(TEST_CAR_ID)).thenReturn(true);
        when(vehicleMapper.softDeleteByCarId(TEST_CAR_ID)).thenReturn(1);

        assertDoesNotThrow(() -> vehicleService.deleteVehicleByCarId(TEST_CAR_ID));

        verify(vehicleMapper).existsByCarId(TEST_CAR_ID);
        verify(vehicleMapper).softDeleteByCarId(TEST_CAR_ID);
    }

    @Test
    void getVehicleWithBatteryType_Success() {
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("vid", TEST_VID);
        expectedResult.put("carId", TEST_CAR_ID);
        
        when(vehicleMapper.findVehicleWithBatteryType(TEST_CAR_ID)).thenReturn(expectedResult);

        Map<String, Object> result = vehicleService.getVehicleWithBatteryType(TEST_CAR_ID);

        assertNotNull(result);
        assertEquals(TEST_VID, result.get("vid"));
        assertEquals(TEST_CAR_ID, result.get("carId"));
        verify(vehicleMapper).findVehicleWithBatteryType(TEST_CAR_ID);
    }

    @Test
    void getBatteryTypeName_Success() {
        String expectedTypeName = "三元锂电池";
        when(vehicleMapper.findBatteryTypeName(TEST_CAR_ID)).thenReturn(expectedTypeName);

        String result = vehicleService.getBatteryTypeName(TEST_CAR_ID);

        assertEquals(expectedTypeName, result);
        verify(vehicleMapper).findBatteryTypeName(TEST_CAR_ID);
    }

    @Test
    void existsActiveVehicle_True() {
        when(vehicleMapper.existsById(TEST_VID)).thenReturn(true);
        assertTrue(vehicleService.existsActiveVehicle(TEST_VID));
        verify(vehicleMapper).existsById(TEST_VID);
    }

    @Test
    void existsActiveVehicleByCarId_True() {
        when(vehicleMapper.existsByCarId(TEST_CAR_ID)).thenReturn(true);
        assertTrue(vehicleService.existsActiveVehicleByCarId(TEST_CAR_ID));
        verify(vehicleMapper).existsByCarId(TEST_CAR_ID);
    }
}
