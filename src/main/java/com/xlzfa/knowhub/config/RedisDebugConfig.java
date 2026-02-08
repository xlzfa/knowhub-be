package com.xlzfa.knowhub.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisDebugConfig {

    @Autowired
    private RedisProperties redisProperties;

    @PostConstruct
    public void printRedisConfig() {
        System.out.println("==== Redis REAL Config ====");
        System.out.println("host = " + redisProperties.getHost());
        System.out.println("port = " + redisProperties.getPort());
        System.out.println("password = " + redisProperties.getPassword());
        System.out.println("database = " + redisProperties.getDatabase());
        System.out.println("===========================");
    }
}
