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

        when(vehicleMapper.existsByCarId(TEST_CAR_ID)).thenReturn(false);
        when(vehicleMapper.save(any(Vehicle.class))).thenReturn(1);

        // 执行测试
        Vehicle result = vehicleService.createVehicle(newVehicle);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getVid());
        assertTrue(result.getVid().startsWith("VH"));
        assertEquals(16, result.getVid().length());
        assertTrue(result.getVid().matches("VH[A-Z0-9]{14}"));
        
        assertEquals(TEST_CAR_ID, result.getCarId());
        assertEquals(1, result.getBatteryTypeId());
        assertEquals(0, result.getTotalMileage());
        assertEquals(100, result.getBatteryHealth());
        assertFalse(result.isDeleted());
        
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertEquals(result.getCreatedAt(), result.getUpdatedAt());
        
        verify(vehicleMapper).save(any(Vehicle.class));
    }

    @Test
    void createVehicle_DuplicateCarId() {
        // 准备测试数据
        when(vehicleMapper.existsByCarId(TEST_CAR_ID)).thenReturn(true);

        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> vehicleService.createVehicle(testVehicle));
        assertEquals("车架号已存在: " + TEST_CAR_ID, exception.getMessage());
        verify(vehicleMapper, never()).save(any());
    }

    @Test
    void getVehicleByVid_Success() {
        // 准备测试数据
        when(vehicleMapper.findById(TEST_VID)).thenReturn(testVehicle);

        // 执行测试
        Vehicle result = vehicleService.getVehicleByVid(TEST_VID);

        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_VID, result.getVid());
        assertEquals(TEST_CAR_ID, result.getCarId());
        verify(vehicleMapper).findById(TEST_VID);
    }

    @Test
    void getVehicleByVid_NotFound() {
        // 准备测试数据
        when(vehicleMapper.findById(TEST_VID)).thenReturn(null);

        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> vehicleService.getVehicleByVid(TEST_VID));
        assertEquals("未找到VID为: " + TEST_VID + " 的车辆", exception.getMessage());
        verify(vehicleMapper).findById(TEST_VID);
    }

    @Test
    void getVehicleByCarId_Success() {
        // 准备测试数据
        when(vehicleMapper.findByCarId(TEST_CAR_ID)).thenReturn(testVehicle);

        // 执行测试
        Vehicle result = vehicleService.getVehicleByCarId(TEST_CAR_ID);

        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_VID, result.getVid());
        assertEquals(TEST_CAR_ID, result.getCarId());
        verify(vehicleMapper).findByCarId(TEST_CAR_ID);
    }

    @Test
    void getVehicleByCarId_NotFound() {
        // 准备测试数据
        when(vehicleMapper.findByCarId(TEST_CAR_ID)).thenReturn(null);

        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> vehicleService.getVehicleByCarId(TEST_CAR_ID));
        assertEquals("未找到车架号为: " + TEST_CAR_ID + " 的车辆", exception.getMessage());
        verify(vehicleMapper).findByCarId(TEST_CAR_ID);
    }

    @Test
    void getAllVehicles_Success() {
        // 准备测试数据
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setVid("VH24F1E2D3C4B5A6");
        vehicle2.setCarId(1002);
        
        when(vehicleMapper.findAll()).thenReturn(Arrays.asList(testVehicle, vehicle2));

        // 执行测试
        List<Vehicle> results = vehicleService.getAllVehicles();

        // 验证结果
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
        updateVehicle.setCarId(TEST_CAR_ID);
        updateVehicle.setBatteryHealth(90);

        when(vehicleMapper.existsById(TEST_VID)).thenReturn(true);
        when(vehicleMapper.findById(TEST_VID)).thenReturn(testVehicle);
        when(vehicleMapper.update(any(Vehicle.class))).thenReturn(1);

        // 执行测试
        Vehicle result = vehicleService.updateVehicle(updateVehicle);

        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_VID, result.getVid());
        assertEquals(90, result.getBatteryHealth());
        assertEquals(testVehicle.getCreatedAt(), result.getCreatedAt());
        assertNotEquals(testVehicle.getUpdatedAt(), result.getUpdatedAt());
        verify(vehicleMapper).update(any(Vehicle.class));
    }

    @Test
    void updateVehicle_NotFound() {
        // 准备测试数据
        when(vehicleMapper.existsById(TEST_VID)).thenReturn(false);

        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> vehicleService.updateVehicle(testVehicle));
        assertEquals("未找到VID为: " + TEST_VID + " 的车辆", exception.getMessage());
        verify(vehicleMapper, never()).update(any());
    }

    @Test
    void updateVehicle_DuplicateCarId() {
        // 准备测试数据
        Vehicle updateVehicle = new Vehicle();
        updateVehicle.setVid(TEST_VID);
        updateVehicle.setCarId(1002); // 新的车架号

        Vehicle existingVehicle = new Vehicle();
        existingVehicle.setVid("VH24F1E2D3C4B5A6");
        existingVehicle.setCarId(1002);

        when(vehicleMapper.existsById(TEST_VID)).thenReturn(true);
        when(vehicleMapper.findById(TEST_VID)).thenReturn(testVehicle);
        when(vehicleMapper.existsByCarId(1002)).thenReturn(true);

        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> vehicleService.updateVehicle(updateVehicle));
        assertEquals("车架号已存在: 1002", exception.getMessage());
        verify(vehicleMapper, never()).update(any());
    }




    @Test
    void existsActiveVehicle_True() {
        // 准备测试数据
        when(vehicleMapper.existsById(TEST_VID)).thenReturn(true);

        // 执行测试
        boolean result = vehicleService.existsActiveVehicle(TEST_VID);

        // 验证结果
        assertTrue(result);
        verify(vehicleMapper).existsById(TEST_VID);
    }

    @Test
    void existsActiveVehicle_False() {
        // 准备测试数据
        when(vehicleMapper.existsById(TEST_VID)).thenReturn(false);

        // 执行测试
        boolean result = vehicleService.existsActiveVehicle(TEST_VID);

        // 验证结果
        assertFalse(result);
        verify(vehicleMapper).existsById(TEST_VID);
    }

    @Test
    void existsActiveVehicleByCarId_True() {
        // 准备测试数据
        when(vehicleMapper.existsByCarId(TEST_CAR_ID)).thenReturn(true);

        // 执行测试
        boolean result = vehicleService.existsActiveVehicleByCarId(TEST_CAR_ID);

        // 验证结果
        assertTrue(result);
        verify(vehicleMapper).existsByCarId(TEST_CAR_ID);
    }

    @Test
    void existsActiveVehicleByCarId_False() {
        // 准备测试数据
        when(vehicleMapper.existsByCarId(TEST_CAR_ID)).thenReturn(false);

        // 执行测试
        boolean result = vehicleService.existsActiveVehicleByCarId(TEST_CAR_ID);

        // 验证结果
        assertFalse(result);
        verify(vehicleMapper).existsByCarId(TEST_CAR_ID);
    }
}
