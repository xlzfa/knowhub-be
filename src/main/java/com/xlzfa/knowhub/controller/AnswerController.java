package com.xlzfa.knowhub.controller;


import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.domain.dto.AnswerAddDto;
import com.xlzfa.knowhub.domain.dto.LikeDto;
import com.xlzfa.knowhub.service.AnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/answer")
@Tag( name = "回答模块")
public class AnswerController {

    @Autowired
    private AnswerService answerService;


    @GetMapping("/feed")
    @Operation( summary = "推荐流")
    public ResponseResult answerFeed(@RequestParam Integer pageNum, @RequestParam Integer pageSize){
        return answerService.answerFeed(pageNum, pageSize);
    }

    @PostMapping("/like")
    @Operation( summary = "点赞功能")
    public ResponseResult answerLike(@RequestBody LikeDto dto){
        return answerService.updateLike(dto.getId(), dto.getLike());
    }

    @PostMapping("add")
    @Operation( summary = "新增回答")
    public ResponseResult addAnswer(@RequestBody AnswerAddDto answerAddDto){
        return answerService.addAnswer(answerAddDto);
    }

    @GetMapping("/mine")
    @Operation( summary = "我的回答")
    public ResponseResult myAnswer(@RequestParam Long userId){
        return answerService.myAnswer(userId);
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/redis/ping")
    public String redisPing() {
        try {
            return stringRedisTemplate.execute(
                    (org.springframework.data.redis.core.RedisCallback<String>)
                            connection -> connection.ping()
            );
        } catch (Exception e) {
            e.printStackTrace(); // ⭐关键
            throw e;
        }
    }


    @Autowired
    private org.springframework.core.env.Environment env;

    @GetMapping("/redis/env")
    public String redisEnv() {
        return "host=" + env.getProperty("spring.redis.host")
                + ", port=" + env.getProperty("spring.redis.port")
                + ", user=" + env.getProperty("spring.redis.username")
                + ", password=" + env.getProperty("spring.redis.password");
    }



}
