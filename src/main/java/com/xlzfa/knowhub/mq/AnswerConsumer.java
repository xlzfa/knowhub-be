package com.xlzfa.knowhub.mq;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xlzfa.knowhub.config.RabbitConfig;
import com.xlzfa.knowhub.dao.AnswerMapper;
import com.xlzfa.knowhub.dao.LikeRecordMapper;
import com.xlzfa.knowhub.domain.pojo.Answer;
import com.xlzfa.knowhub.domain.pojo.LikeRecord;
import com.xlzfa.knowhub.service.AnswerService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@RabbitListener(queues = RabbitConfig.ANSWER_LIKE_QUEUE,
        containerFactory = "rabbitListenerContainerFactory")
public class AnswerConsumer {


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AnswerService answerService;



    @RabbitHandler
    public void process(Map<String, Object> msg) {



        Long userId = Long.parseLong(msg.get("userId").toString());
        Long id = Long.parseLong(msg.get("targetId").toString());
        Integer liked = Integer.parseInt(msg.get("liked").toString());


        throw new RuntimeException("test DLQ");


//        try {
//
//
//            answerService.likeSql(userId,id,liked);
//
//            redisTemplate.opsForSet().remove("answer:like:dirty", id.toString());
//
//        } catch (Exception e) {
//            throw e;
//    }



    }



}
