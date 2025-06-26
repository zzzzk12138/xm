package com.xiaomi.bms.mq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author ：
 * @description:
 **/
@Component
public class SpringProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    public void sendMessage(String topic,String msg){
        this.rocketMQTemplate.convertAndSend(topic,msg);
    }
}