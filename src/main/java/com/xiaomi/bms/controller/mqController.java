package com.xiaomi.bms.controller;

import com.xiaomi.bms.mq.SpringProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class mqController {

    @Autowired
    private SpringProducer springProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private com.xiaomi.bms.service.productService productService;

    @GetMapping("/send/{id}")
    public long mqTest(@PathVariable Integer id){
        log.info("查询id为{}的商品信息",id);
        String key = "product::"+id;
        Long stock;

        stock = (Long) redisTemplate.opsForValue().get(key);
        if (stock != null) {
            log.info("从Redis获取商品{}库存: stock{}", id, stock);
        } else {
            // 第一级：查询数据库
            stock = productService.getStockById(id);
            if (stock != null) {
                // 回填到Redis和本地缓存
                redisTemplate.opsForValue().set(key, stock, 60, TimeUnit.SECONDS); // 60s过期
                log.info("从数据库获取商品，id: {} 的库存为: {}", id, stock);
            }
        }
        
        // 无论从Redis还是数据库获取，都发送消息
        String msg = "mqTest" + stock;
        springProducer.sendMessage("TestTopic",msg);
        return stock;
    }
}
