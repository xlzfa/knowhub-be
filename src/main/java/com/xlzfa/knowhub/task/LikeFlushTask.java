package com.xlzfa.knowhub.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xlzfa.knowhub.dao.AnswerMapper;
import com.xlzfa.knowhub.dao.LikeRecordMapper;
import com.xlzfa.knowhub.domain.pojo.Answer;
import com.xlzfa.knowhub.domain.pojo.LikeRecord;
import com.xlzfa.knowhub.service.AnswerService;
import com.xlzfa.knowhub.service.LikeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    @Autowired
    private LikeRecordService likeRecordService;

    private static final String DIRTY_KEY = "answer:like:dirty";
    private static final String USER_KEY = "answer:like:users:";
    private static final String COUNT_KEY = "answer:like:count:";


    @Scheduled(cron = "0 * * * * ?") // 每分钟执行
    @Transactional
    public void fallbackSync() {

        Set<String> dirtySet = stringRedisTemplate.opsForSet().members(DIRTY_KEY);

        if (dirtySet == null || dirtySet.isEmpty()) {
            return;
        }


        for (String StringanswerId : dirtySet) {
            try {

                Long answerId = Long.parseLong(StringanswerId);


                //处理add
                String addKey = "answer:like:add:" + answerId;


                List<String> addRedisRecords = stringRedisTemplate.opsForSet().pop(addKey,1000);

                List<LikeRecord> addRecords = new ArrayList<>();

                while (addRedisRecords != null && !addRedisRecords.isEmpty()) {
                    for (String record : addRedisRecords) {

                        String[] parts = record.split(":");
                        long uid = Long.parseLong(parts[1]);

                        addRecords.add(LikeRecord.builder()
                                .userId(uid)
                                .targetId(answerId)
                                .targetType(1)
                                .build());

                    }
                    likeRecordService.saveBatch(addRecords, 1000);
                    addRecords.clear();
                    addRedisRecords = stringRedisTemplate.opsForSet().pop(addKey,1000);

                }

                //处理remove

                String removeKey = "answer:like:remove:" + answerId;

                List<String> removeRedisRecords = stringRedisTemplate.opsForSet().pop(removeKey,1000);

                List<Long> removeUserIds = new ArrayList<>();

                while (removeRedisRecords != null && !removeRedisRecords.isEmpty()) {
                    for (String record : removeRedisRecords) {

                        String[] parts = record.split(":");
                        long uid = Long.parseLong(parts[1]);

                        removeUserIds.add(uid);

                    }
                    likeRecordMapper.delete(
                            new LambdaQueryWrapper<LikeRecord>()
                                    .eq(LikeRecord::getTargetId, answerId)
                                    .eq(LikeRecord::getTargetType, 1)
                                    .in(LikeRecord::getUserId, removeUserIds)
                    );
                    removeUserIds.clear();
                    removeRedisRecords = stringRedisTemplate.opsForSet().pop(removeKey,1000);

                }

                String countKey = COUNT_KEY + answerId;

                String count = stringRedisTemplate.opsForValue().get(countKey);

                Long likeCount = (count == null) ? 0 : Long.parseLong(count);

                answerMapper.update(
                        null,
                        new UpdateWrapper<Answer>()
                                .set("like_count", likeCount)
                                .eq("id", answerId)
                );


                stringRedisTemplate.opsForSet().remove(DIRTY_KEY, answerId);

            } catch (Exception e) {
                System.err.println("定时任务落库失败 answerId=" + StringanswerId);
                e.printStackTrace();
            }
        }



    }



}
