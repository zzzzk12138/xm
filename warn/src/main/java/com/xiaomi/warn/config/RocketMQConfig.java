package com.xiaomi.warn.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

@Configuration
@Slf4j
public class RocketMQConfig {

    @Bean
    public MappingJackson2MessageConverter rocketMQMessageConverter() {
        log.info("配置RocketMQ消息转换器");
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 启用默认类型信息
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        
        converter.setObjectMapper(objectMapper);
        log.info("RocketMQ消息转换器配置完成");
        return converter;
    }
} 