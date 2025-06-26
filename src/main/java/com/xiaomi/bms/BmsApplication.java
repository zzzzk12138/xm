package com.xiaomi.bms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.xiaomi.bms.mapper")
@EnableTransactionManagement
public class BmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BmsApplication.class, args);
    }

}
