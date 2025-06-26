package com.xiaomi.warn.task;

import com.xiaomi.warn.entity.Vehicle;
import com.xiaomi.warn.service.vehicleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class warnSignalProviderTask {
    
    @Autowired
    private vehicleService vehicleService;
    
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    
    private static final String TOPIC = "SignalStatusTopic";
    
    /**
     * 发送VID到消息队列
     * @param carIds 车辆ID列表
     */
    public void sendVidsToMQ(Set<Integer> carIds) {
        if (carIds == null || carIds.isEmpty()) {
            return;
        }

        try {
            // 直接批量查询所有carId对应的vid
            Set<String> vids = vehicleService.getVidsByCarIds(carIds);
            
            if (!vids.isEmpty()) {
                rocketMQTemplate.syncSend(TOPIC, MessageBuilder.withPayload(vids).build());
                log.info("成功发送{}个VID到MQ: {}", vids.size(), vids);
            }
        } catch (Exception e) {
            log.error("发送VID到MQ失败: {}", e.getMessage(), e);
            throw new RuntimeException("发送VID到MQ失败", e);
        }
    }
}
