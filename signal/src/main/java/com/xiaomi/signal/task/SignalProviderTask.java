package com.xiaomi.signal.task;

import com.xiaomi.signal.entity.Signal;
import com.xiaomi.signal.service.signalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SignalProviderTask {

    @Autowired
    private signalService signalService;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    private static final String TOPIC = "SignalTopic";

    /**
     * 每20秒执行一次，查询未删除的信号并批量发送到RocketMQ
     */
    @Scheduled(fixedRate = 20000)
    public void sendSignals() {
        log.info("开始执行信号批量发送任务");
        try {
            // 获取所有信号
            List<Signal> signals = signalService.getAllSignals();
            
            // 过滤出状态正常的信号
            List<Signal> activeSignals = signals.stream()
                    .filter(signal -> signal.getStatus() == 0)
                    .collect(Collectors.toList());

            if (!activeSignals.isEmpty()) {
                try {
                    // 批量发送消息
                    rocketMQTemplate.syncSend(TOPIC, MessageBuilder.withPayload(activeSignals).build());
                    log.info("成功批量发送信号到RocketMQ, 发送数量: {}", activeSignals.size());
                } catch (Exception e) {
                    log.error("批量发送信号到RocketMQ失败: error={}", e.getMessage(), e);
                }
            } else {
                log.info("没有需要发送的有效信号");
            }
            
            log.info("信号批量发送任务执行完成，总信号数: {}, 有效信号数: {}", signals.size(), activeSignals.size());
        } catch (Exception e) {
            log.error("执行信号批量发送任务时发生错误: {}", e.getMessage(), e);
        }
    }
}
