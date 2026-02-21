package com.xlzfa.knowhub.mq;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xlzfa.knowhub.config.RabbitConfig;
import com.xlzfa.knowhub.dao.AnswerMapper;
import com.xlzfa.knowhub.dao.LikeRecordMapper;
import com.xlzfa.knowhub.domain.pojo.Answer;
import com.xlzfa.knowhub.domain.pojo.LikeRecord;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = RabbitConfig.ANSWER_LIKE_QUEUE,
        containerFactory = "rabbitListenerContainerFactory")
public class AnswerConsumer {


    @Autowired
    private LikeRecordMapper likeRecordMapper;

    @Autowired
    private AnswerMapper answerMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;



    @RabbitHandler
    public void process(Map<String, Object> msg) {



        Long userId = Long.parseLong(msg.get("userId").toString());
        Long id = Long.parseLong(msg.get("targetId").toString());
        Integer liked = Integer.parseInt(msg.get("liked").toString());



        try {
            if(liked == 1){
                LikeRecord build = LikeRecord.builder()
                        .userId(userId)
                        .targetId(id)
                        .targetType(1)
                        .build();
                likeRecordMapper.insert(build);
                answerMapper.update(
                        null,
                        new UpdateWrapper<Answer>()
                                .setSql("like_count = like_count + 1")
                                .eq("id", id)
                );
            }else {
                likeRecordMapper.delete(
                        new LambdaQueryWrapper<LikeRecord>()
                                .eq(LikeRecord::getUserId, userId)
                                .eq(LikeRecord::getTargetId, id)
                                .eq(LikeRecord::getTargetType, 1)
                );
                answerMapper.update(
                        null,
                        new UpdateWrapper<Answer>()
                                .setSql("like_count = IF(like_count > 0, like_count - 1, 0)")
                                .eq("id", id)
                );
            }

            redisTemplate.opsForSet().remove("answer:like:dirty", id.toString());

        } catch (Exception e) {
        e.printStackTrace();
        // MQ 会自动重试
    }



    }


}
