package com.xlzfa.knowhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.common.SystemConstants;
import com.xlzfa.knowhub.dao.AnswerMapper;
import com.xlzfa.knowhub.dao.CommentMapper;
import com.xlzfa.knowhub.dao.LikeRecordMapper;
import com.xlzfa.knowhub.domain.dto.AnswerAddDto;
import com.xlzfa.knowhub.domain.pojo.*;
import com.xlzfa.knowhub.domain.vo.AnswerVo;
import com.xlzfa.knowhub.domain.vo.CommentVo;
import com.xlzfa.knowhub.domain.vo.LikeVo;
import com.xlzfa.knowhub.domain.vo.PageVo;
import com.xlzfa.knowhub.service.AnswerService;
import com.xlzfa.knowhub.service.QuestionService;
import com.xlzfa.knowhub.service.UserService;
import com.xlzfa.knowhub.util.BaseContext;
import com.xlzfa.knowhub.util.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AnswerServiceImpl extends ServiceImpl<AnswerMapper, Answer> implements AnswerService {

    @Autowired
    private AnswerMapper answerMapper;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private LikeRecordMapper likeRecordMapper;

    @Override
    public ResponseResult answerFeed(Integer pageNum, Integer pageSize) {

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 50) {
            pageSize = 10;
        }


        Page<Answer> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Answer> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Answer::getStatus, SystemConstants.ANSWER_STATUS_NORMAL);

        //推荐暂时这么做
        wrapper.orderByDesc(Answer::getIsAccepted); // 已采纳的更值钱
        wrapper.orderByDesc(Answer::getLikeCount);  // 点赞多的优先
        wrapper.orderByDesc(Answer::getCreateTime);// 新回答兜底

        answerMapper.selectPage(page, wrapper);

        List<AnswerVo> vos =
                BeanCopyUtils.copyBeanList(page.getRecords(), AnswerVo.class);

        vos.forEach( vo ->{
            //TODO 后期优化
            Question question = questionService.getById(vo.getQuestionId());
            if (question != null){
                vo.setQuertionTitle(question.getTitle());
            }

        });

        vos.forEach( vo ->{
            //TODO 后期优化
            User user = userService.getById(vo.getUserId());
            if (user != null){
                vo.setUser(user.getUsername());
            }

            //只返回前三条
            PageVo<CommentVo> commentPage = commentPage(vo.getId(), 1, 3);

            vo.setComments(commentPage);

        });

        PageVo pageVo = new PageVo(vos, page.getTotal());

        return ResponseResult.success(pageVo);

    }

    @Override
    public ResponseResult updateLike(Long id, boolean like) {

        //TODO 后期先装redis，定时写入mysql

        Long userId = BaseContext.getCurrentId();

        boolean res = true;

        //如果是要点赞
        if (like){

            Long islike = findLikeRecord(userId, id);

            if (islike > 0L){

                this.lambdaUpdate()
                        .setSql("like_count = IF(like_count > 0, like_count - 1, 0)")
                        .eq(Answer::getId, id)
                        .update();

                likeRecordMapper.deleteById(islike);
                res = false;
            }else {
                this.lambdaUpdate()
                        .setSql("like_count = like_count + 1")
                        .eq(Answer::getId, id)
                        .update();

                LikeRecord newlike = LikeRecord.builder()
                        .userId(userId)
                        .targetType(1)
                        .targetId(id)
                        .build();

                likeRecordMapper.insert(newlike);
            }


        }else {

            Long islike = findLikeRecord(userId, id);

            if (islike == 0L){

                this.lambdaUpdate()
                        .setSql("like_count = like_count + 1")
                        .eq(Answer::getId, id)
                        .update();

                LikeRecord newlike = LikeRecord.builder()
                        .userId(userId)
                        .targetType(1)
                        .targetId(id)
                        .build();

                likeRecordMapper.insert(newlike);
            }else {

                this.lambdaUpdate()
                        .setSql("like_count = IF(like_count > 0, like_count - 1, 0)")
                        .eq(Answer::getId, id)
                        .update();

                likeRecordMapper.deleteById(islike);
                res = false;
            }


        }

        return ResponseResult.success(res);


    }

    public Long findLikeRecord(Long userId, Long answerId){

        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(LikeRecord::getTargetType, 1)
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetId, answerId);

        if (likeRecordMapper.selectOne(wrapper) != null){
            return likeRecordMapper.selectOne(wrapper).getId();
        }else {
            return 0L;
        }



    }

    @Override
    public ResponseResult addAnswer(AnswerAddDto answerAddDto) {

        Answer answer = Answer.builder()
                .questionId(answerAddDto.getQuestionId())
                .status(SystemConstants.ANSWER_STATUS_NORMAL)
                .userId(answerAddDto.getUserId())
                .content(answerAddDto.getContent())
                .likeCount(0L)
                .isAccepted(0)
                .build();


        baseMapper.insert(answer);

        return ResponseResult.success(answer.getId());
    }

    @Override
    public ResponseResult myAnswer(Long userId) {

        LambdaQueryWrapper<Answer> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Answer::getUserId, userId)
                .eq(Answer::getStatus,SystemConstants.ANSWER_STATUS_NORMAL)
                .orderByDesc(Answer::getCreateTime);

        List<Answer> answers = list(wrapper);

        List<AnswerVo> vos = BeanCopyUtils.copyBeanList(answers, AnswerVo.class);

        vos.forEach( vo ->{
            //TODO 后期优化
            Question question = questionService.getById(vo.getQuestionId());
            if (question != null){
                vo.setQuertionTitle(question.getTitle());
            }

        });

        User user = userService.getById(userId);

        vos.forEach( vo ->{
            //TODO 后期优化
            if (user != null){
                vo.setUser(user.getUsername());
            }

        });

        return ResponseResult.success(vos);



    }

    public PageVo<CommentVo> commentPage(Long id, Integer pageNum, Integer pageSize){

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 50) {
            pageSize = 10;
        }


        Page<Comment> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Comment::getAnswerId, id);
        wrapper.eq(Comment::getParentId, -1);

        wrapper.orderByDesc(Comment::getCreateTime);// 新回答兜底

        commentMapper.selectPage(page, wrapper);

        List<CommentVo> vos =
                BeanCopyUtils.copyBeanList(page.getRecords(), CommentVo.class);


        vos.forEach( vo ->{
            //TODO 后期优化
            User user = userService.getById(vo.getUserId());
            if (user != null){
                vo.setUsername(user.getUsername());
                vo.setUserId(user.getId());
            }

        });

        PageVo pageVo = new PageVo(vos, page.getTotal());

        return pageVo;

    }





}
