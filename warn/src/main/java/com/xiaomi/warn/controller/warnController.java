package com.xiaomi.warn.controller;

import com.xiaomi.warn.dto.ResponseResult;
import com.xiaomi.warn.dto.warnDTO;
import com.xiaomi.warn.service.warnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/warn")
public class warnController {

    @Autowired
    private warnService warnService;

    @PostMapping()
    public Map<String, Object> receiveWarns(@Valid @RequestBody List<warnDTO> warnDTOList) {
        log.info("接收到警告信息列表, 数量: {}", warnDTOList.size());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Map<String, Object>> results = warnService.processWarns(warnDTOList);
            
            response.put("status", 200);
            response.put("msg", "ok");
            response.put("data", results);
            
        } catch (Exception e) {
            log.error("处理警告信息失败: {}", e.getMessage(), e);
            
            response.put("status", 500);
            response.put("msg", e.getMessage());
            response.put("data", null);
        }

        System.out.println(response.toString());
        return response;
    }

    /**
     * 查询指定车辆的预警信息
     * @param carId 车辆ID
     * @return 预警信息列表
     */
    @GetMapping("/vehicle/{carId}")
    public ResponseResult<List<Map<String, Object>>> getVehicleWarns(@PathVariable Integer carId) {
        try {
            List<Map<String, Object>> warns = warnService.getWarnsByCarId(carId);
            return ResponseResult.success(warns);
        } catch (Exception e) {
            log.error("查询车辆预警信息失败: carId={}, error={}", carId, e.getMessage(), e);
            return ResponseResult.error("查询车辆预警信息失败: " + e.getMessage());
        }
    }
}
