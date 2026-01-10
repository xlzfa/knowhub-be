package com.xlzfa.knowhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.dao.CommentMapper;
import com.xlzfa.knowhub.domain.dto.CommentAddDto;
import com.xlzfa.knowhub.domain.pojo.Comment;
import com.xlzfa.knowhub.domain.pojo.User;
import com.xlzfa.knowhub.domain.vo.AnswerVo;
import com.xlzfa.knowhub.domain.vo.CommentVo;
import com.xlzfa.knowhub.domain.vo.PageVo;
import com.xlzfa.knowhub.service.CommentService;
import com.xlzfa.knowhub.service.UserService;
import com.xlzfa.knowhub.util.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {


    @Autowired
    private UserService userService;

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

    @Override
    public ResponseResult list(Long answerId) {



        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Comment::getAnswerId,answerId);


        List<Comment> list = baseMapper.selectList(wrapper);

        List<CommentVo> vos =
                BeanCopyUtils.copyBeanList(list, CommentVo.class);


        vos.forEach( vo ->{
            //TODO 后期优化
            User user = userService.getById(vo.getUserId());
            if (user != null){
                vo.setUsername(user.getUsername());
                vo.setUserId(user.getId());
            }
        });

        return ResponseResult.success(vos);
    }
}
