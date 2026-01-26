package com.xlzfa.knowhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.dao.AnswerMapper;
import com.xlzfa.knowhub.dao.CommentMapper;
import com.xlzfa.knowhub.domain.dto.CommentAddDto;
import com.xlzfa.knowhub.domain.pojo.Answer;
import com.xlzfa.knowhub.domain.pojo.Comment;
import com.xlzfa.knowhub.domain.pojo.Question;
import com.xlzfa.knowhub.domain.pojo.User;
import com.xlzfa.knowhub.domain.vo.AnswerVo;
import com.xlzfa.knowhub.domain.vo.CommentVo;
import com.xlzfa.knowhub.domain.vo.PageVo;
import com.xlzfa.knowhub.service.AnswerService;
import com.xlzfa.knowhub.service.CommentService;
import com.xlzfa.knowhub.service.QuestionService;
import com.xlzfa.knowhub.service.UserService;
import com.xlzfa.knowhub.util.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {


    @Autowired
    private UserService userService;

    @Autowired
    private AnswerService answerService;
    @Autowired
    private AnswerMapper answerMapper;
    @Autowired
    private QuestionService questionService;

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


        //查用户名
        Set<Long> userIds = vos.stream()
                .map(CommentVo::getUserId)
                .collect(Collectors.toSet());

        List<User> users = userService.listByIds(userIds);

        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));


        vos.forEach( vo ->{


            User user = userMap.get(vo.getUserId());

            if (user != null){
                vo.setUsername(user != null ? user.getUsername() : "匿名");
            }

        });

        return ResponseResult.success(vos);
    }

    @Override
    public ResponseResult myComment(Long userId) {

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Comment::getUserId,userId);


        List<Comment> list = baseMapper.selectList(wrapper);

        List<CommentVo> vos =
                BeanCopyUtils.copyBeanList(list, CommentVo.class);


        User user = userService.getById(userId);

        vos.forEach( vo ->{
            if (user != null){
                vo.setUsername(user.getUsername());
                vo.setUserId(user.getId());
            }
            Long answerId = vo.getAnswerId();
            Answer answer = answerService.getById(answerId);
            if (answer != null){
                vo.setAnswerUsername(getAnswerUsername(answerId));
                vo.setAnswerUserId(getAnswerUserId(answerId));
                vo.setQuestionTitle(getQuestionTitle(answerId));
                vo.setQuestionId(getQuestionId(answerId));
            }
        });

        return ResponseResult.success(vos);



    }


    public String getAnswerUsername(Long id){

        Answer answer = answerMapper.selectById(id);
        Long userId = answer.getUserId();
        User user = userService.getById(userId);
        return user.getUsername();

    }

    public Long getAnswerUserId(Long id){
        Answer answer = answerMapper.selectById(id);
        return answer.getUserId();
    }

    public String getQuestionTitle(Long id){

        Answer answer = answerMapper.selectById(id);
        Question question = questionService.getById(answer.getQuestionId());
        return question.getTitle();

    }

    public Long getQuestionId(Long id){
        Answer answer = answerMapper.selectById(id);
        return answer.getQuestionId();
    }
}
