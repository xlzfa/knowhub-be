package com.xlzfa.knowhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.domain.dto.CommentAddDto;
import com.xlzfa.knowhub.domain.pojo.Comment;

public interface CommentService extends IService<Comment> {
    ResponseResult add(CommentAddDto commentAddDto);
}
