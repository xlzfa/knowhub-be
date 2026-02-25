package com.xlzfa.knowhub.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xlzfa.knowhub.dao.AnswerMapper;
import com.xlzfa.knowhub.dao.LikeRecordMapper;
import com.xlzfa.knowhub.domain.pojo.Answer;
import com.xlzfa.knowhub.domain.pojo.LikeRecord;
import com.xlzfa.knowhub.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class LikeFlushTask {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private AnswerMapper answerMapper;

    @Autowired
    private LikeRecordMapper likeRecordMapper;

    private static final String DIRTY_KEY = "answer:like:dirty";
    private static final String USER_KEY = "answer:like:users:";
    private static final String COUNT_KEY = "answer:like:count:";


    @Scheduled(cron = "0 * * * * ?") // 每分钟执行
    public void fallbackSync() {

        Set<String> dirtySet = stringRedisTemplate.opsForSet().members(DIRTY_KEY);

        if (dirtySet == null || dirtySet.isEmpty()) {
            return;
        }

        for (String answerId : dirtySet) {
            try {
                String userKey = USER_KEY + answerId;
                Set<String> userSet = stringRedisTemplate.opsForSet().members(userKey);

                String countKey = COUNT_KEY + answerId;

                String count = stringRedisTemplate.opsForValue().get(countKey);

                Long likeCount = (count == null) ? 0 : Long.parseLong(count);

                answerMapper.update(
                        null,
                        new UpdateWrapper<Answer>()
                                .set("like_count", likeCount)
                                .eq("id", answerId)
                );

                likeRecordMapper.delete(new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getTargetId, answerId)
                        .eq(LikeRecord::getTargetType, 1)
                );


                if (userSet != null && !userSet.isEmpty()) {
                    List<LikeRecord> records = userSet.stream()
                            .map(uidStr -> LikeRecord.builder()
                                    .userId(Long.parseLong(uidStr))
                                    .targetId(Long.parseLong(answerId))
                                    .targetType(1)
                                    .build())
                            .toList();
                    // 批量插入
                    records.forEach(likeRecordMapper::insert);
                }



                stringRedisTemplate.opsForSet().remove(DIRTY_KEY, answerId);
            } catch (Exception e) {
                System.err.println("定时任务落库失败 answerId=" + answerId);
                e.printStackTrace();
            }
        }



    }



}
