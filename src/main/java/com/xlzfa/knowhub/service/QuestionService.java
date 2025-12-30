package com.xlzfa.knowhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.domain.dto.QuestionAddDto;
import com.xlzfa.knowhub.domain.pojo.Question;

public interface QuestionService extends IService<Question> {
    ResponseResult questionDetail(Long id, Integer pageNum, Integer pageSize);

    ResponseResult questionInfo(Long id);

    ResponseResult updateLike(Long id, Boolean like);

    ResponseResult addQuestion(QuestionAddDto questionAddDto);
}
