package com.xlzfa.knowhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.domain.entity.Answer;
import org.springframework.web.bind.annotation.RequestParam;

public interface AnswerService extends IService<Answer> {
    ResponseResult answerFeed(Integer pageNum, Integer pageSize);

    ResponseResult updateLike(Long id, boolean like);
}
