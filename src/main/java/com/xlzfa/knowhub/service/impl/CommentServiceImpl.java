package com.xlzfa.knowhub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.dao.CommentMapper;
import com.xlzfa.knowhub.domain.dto.CommentAddDto;
import com.xlzfa.knowhub.domain.pojo.Comment;
import com.xlzfa.knowhub.service.CommentService;
import org.springframework.stereotype.Service;


@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {


    @Override
    public ResponseResult add(CommentAddDto commentAddDto) {

        Comment comment = Comment.builder()
                .userId(commentAddDto.getUserId())
                .answerId(commentAddDto.getAnswerId())
                .parentId(commentAddDto.getParentId())
                .content(commentAddDto.getContent())
                .build();

        baseMapper.insert(comment);


        return ResponseResult.success(comment.getId());

    }
}
