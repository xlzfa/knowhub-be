package com.xlzfa.knowhub.task;

import com.xlzfa.knowhub.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class LikeFlushTask {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AnswerService answerService;

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
                if (userSet == null) continue;

                // 假设我们记录了全量点赞状态，每个用户都算 liked=1
                for (String userIdStr : userSet) {
                    answerService.likeSql(Long.parseLong(userIdStr), Long.parseLong(answerId), 1);
                }

                // 处理取消点赞的用户：可以用一个单独集合，或在每个用户的 liked 状态里记录 liked=0
                // 这里简单示例，如果你有取消记录，记得遍历处理 liked=0

                // 清理 dirty 标记
                stringRedisTemplate.opsForSet().remove(DIRTY_KEY, answerId);
            } catch (Exception e) {
                System.err.println("定时任务落库失败 answerId=" + answerId);
                e.printStackTrace();
            }
        }



    }



}
