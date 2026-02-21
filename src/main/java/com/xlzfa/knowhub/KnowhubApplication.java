package com.xlzfa.knowhub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.xlzfa.knowhub.dao")
public class KnowhubApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowhubApplication.class, args);
    }

}
