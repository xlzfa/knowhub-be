package com.xlzfa.knowhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.domain.entity.Question;

public interface QuestionService extends IService<Question> {
    ResponseResult questionDetail(Long id, Integer pageNum, Integer pageSize);
}
