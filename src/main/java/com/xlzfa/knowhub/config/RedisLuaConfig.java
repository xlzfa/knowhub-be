package com.xlzfa.knowhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

@Configuration
public class RedisLuaConfig {

    @Bean
    public DefaultRedisScript<List> likeLuaScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(
                "-- KEYS[1]: 用户点赞集合\n" +
                        "-- KEYS[2]: 点赞数\n" +
                        "-- KEYS[3]: dirty set\n" +
                        "-- KEYS[4]: add set\n" +
                        "-- KEYS[5]: remove set\n" +
                        "-- ARGV[1]: userId\n" +
                        "-- ARGV[2]: answerId\n" +
                        "-- ARGV[3]: like (1=点赞, 0=取消)\n" +

                        "-- 安全获取 count（防 nil）\n" +
                        "local function getCount(key)\n" +
                        "   local c = redis.call('GET', key)\n" +
                        "   if not c then\n" +
                        "       return 0\n" +
                        "   end\n" +
                        "   return tonumber(c)\n" +
                        "end\n" +

                        "local userId = ARGV[1]\n" +
                        "local answerId = ARGV[2]\n" +
                        "local like = tonumber(ARGV[3])\n" +
                        "local recordKey = answerId .. ':' .. userId\n" +

                        "if like == 1 then\n" +
                        "   -- 点赞\n" +
                        "   if redis.call('SISMEMBER', KEYS[1], userId) == 0 then\n" +
                        "       redis.call('SADD', KEYS[1], userId)\n" +
                        "       local count = redis.call('INCR', KEYS[2])\n" +
                        "       redis.call('SADD', KEYS[4], recordKey)\n" +
                        "       redis.call('SREM', KEYS[5], recordKey)\n" +
                        "       redis.call('SADD', KEYS[3], answerId)\n" +
                        "       return {1, count}\n" +
                        "   else\n" +
                        "       local count = getCount(KEYS[2])\n" +
                        "       return {1, count}\n" +
                        "   end\n" +
                        "else\n" +
                        "   -- 取消点赞\n" +
                        "   if redis.call('SISMEMBER', KEYS[1], userId) == 1 then\n" +
                        "       redis.call('SREM', KEYS[1], userId)\n" +
                        "       local count = redis.call('DECR', KEYS[2])\n" +
                        "       if count < 0 then\n" +
                        "           redis.call('SET', KEYS[2], 0)\n" +
                        "           count = 0\n" +
                        "       end\n" +
                        "       redis.call('SADD', KEYS[5], recordKey)\n" +
                        "       redis.call('SREM', KEYS[4], recordKey)\n" +
                        "       redis.call('SADD', KEYS[3], answerId)\n" +
                        "       return {0, count}\n" +
                        "   else\n" +
                        "       local count = getCount(KEYS[2])\n" +
                        "       return {0, count}\n" +
                        "   end\n" +
                        "end"
        );
        script.setResultType(List.class);
        return script;
    }
}