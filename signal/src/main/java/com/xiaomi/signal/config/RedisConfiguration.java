package com.xiaomi.signal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.integration.redis.util.RedisLockRegistry;
import java.math.BigDecimal;

@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 注册 JavaTimeModule 以支持 Java 8 日期/时间类型
        objectMapper.registerModule(new JavaTimeModule());
        
        // 配置反序列化特性
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        
        // 注册BigDecimal的处理模块
        SimpleModule module = new SimpleModule();
        module.addDeserializer(BigDecimal.class, new BigDecimalDeserializer());
        objectMapper.registerModule(module);
        
        // 启用默认类型信息
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        
        return objectMapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, 
            ObjectMapper objectMapper) {
        log.info("开始创建redis模板对象");
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);

        // 使用GenericJackson2JsonRedisSerializer来序列化和反序列化redis的value值
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        redisTemplate.setValueSerializer(jsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);

        redisTemplate.afterPropertiesSet();
        log.info("redis模板对象创建成功");
        return redisTemplate;
    }

    @Bean
    public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建redis分布式锁注册器");
        RedisLockRegistry lockRegistry = new RedisLockRegistry(redisConnectionFactory, "signal-locks", 10000L);
        log.info("redis分布式锁注册器创建成功");
        return lockRegistry;
    }
}
