package com.xiaomi.signal.task;

import com.xiaomi.signal.mapper.signalMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RocketMQMessageListener(
    topic = "SignalStatusTopic",
    consumerGroup = "signal-status-consumer-group"
)
public class warnSignalConsumerTask implements RocketMQListener<Set<String>> {
    
    @Autowired
    private signalMapper signalMapper;

    /**
     * 接收 warn 发送来的vid信息，根据vid更新对应的signal表里的状态，防止后续再次发送
     * @param vids
     */
    @Override
    public void onMessage(Set<String> vids) {
        if (vids == null || vids.isEmpty()) {
            log.info("收到空的VID集合");
            return;
        }
        
        log.info("收到VID集合，开始更新信号状态, VID数量: {}", vids.size());
        
        try {
            // 批量更新信号状态
            int updatedCount = signalMapper.updateSignalStatusByVids(vids);
            log.info("成功更新信号状态，更新记录数: {}", updatedCount);
        } catch (Exception e) {
            log.error("更新信号状态失败: {}", e.getMessage(), e);
        }
    }
}
