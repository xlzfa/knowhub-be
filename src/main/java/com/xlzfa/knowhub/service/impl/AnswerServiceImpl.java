package com.xlzfa.knowhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.dao.AnswerMapper;
import com.xlzfa.knowhub.domain.entity.Answer;
import com.xlzfa.knowhub.service.AnswerService;
import org.springframework.stereotype.Service;


@Service
public class AnswerServiceImpl extends ServiceImpl<AnswerMapper, Answer> implements AnswerService {


    @Override
    public ResponseResult answerFeed(Integer pageNum, Integer pageSize) {




    }
}
