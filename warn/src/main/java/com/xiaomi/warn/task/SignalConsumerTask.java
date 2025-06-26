package com.xiaomi.warn.task;

import com.xiaomi.warn.dto.warnDTO;
import com.xiaomi.warn.entity.Signal;
import com.xiaomi.warn.service.warnService;
import com.xiaomi.warn.service.vehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = "SignalTopic",
        consumerGroup = "warn-consumer-group",
        maxReconsumeTimes = 3
)
public class SignalConsumerTask implements RocketMQListener<List<Signal>> {

    @Autowired
    private warnService warnService;

    @Autowired
    private vehicleService vehicleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(List<Signal> signals) {
        log.info("接收到批量信号消息，数量: {}", signals.size());
        
        try {
            List<warnDTO> warnDTOList = new ArrayList<>();
            
            // 处理每个信号
            for (Signal signal : signals) {
                try {
                    // 构建预警DTO
                    warnDTO warn = new warnDTO();
                    
                    // 获取carId 车架号
                    Integer carId = vehicleService.getCarIdByVid(signal.getVid());
                    warn.setCarId(carId);
                    
                    // 构建信号数据
                    Map<String, Double> signalData = new HashMap<>();
                    signalData.put("Mx", signal.getMaxVoltage().doubleValue());
                    signalData.put("Mi", signal.getMinVoltage().doubleValue());
                    signalData.put("Ix", signal.getMaxCurrent().doubleValue());
                    signalData.put("Ii", signal.getMinCurrent().doubleValue());
                    
                    // 将信号数据转换为JSON字符串
                    String signalJson = objectMapper.writeValueAsString(signalData);
                    warn.setSignal(signalJson);
                    
                    warnDTOList.add(warn);
                    log.debug("成功构建预警DTO: signal={}, warn={}", signal, warn);
                } catch (Exception e) {
                    log.error("处理单个信号时发生错误: signal={}, error={}", signal, e.getMessage(), e);
                }
            }
            
            if (!warnDTOList.isEmpty()) {
                // 批量处理预警
                List<Map<String, Object>> results = warnService.processWarns(warnDTOList);
                log.info("预警处理完成，处理结果数量: {}", results.size());
                
                // 打印处理结果
                for (Map<String, Object> result : results) {
                    if (result.containsKey("error")) {
                        log.error("预警处理出现错误: {}", result);
                    } else {
                        log.info("预警处理结果: {}", result);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("批量处理信号消息时发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("处理信号消息失败", e);
        }
    }
}
