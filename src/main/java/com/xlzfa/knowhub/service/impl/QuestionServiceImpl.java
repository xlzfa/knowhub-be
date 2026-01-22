package com.xlzfa.knowhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.common.SystemConstants;
import com.xlzfa.knowhub.dao.AnswerMapper;
import com.xlzfa.knowhub.dao.CommentMapper;
import com.xlzfa.knowhub.dao.LikeRecordMapper;
import com.xlzfa.knowhub.dao.QuestionMapper;
import com.xlzfa.knowhub.domain.dto.QuestionAddDto;
import com.xlzfa.knowhub.domain.pojo.*;
import com.xlzfa.knowhub.domain.vo.*;
import com.xlzfa.knowhub.service.CommentService;
import com.xlzfa.knowhub.service.QuestionService;
import com.xlzfa.knowhub.service.UserService;
import com.xlzfa.knowhub.util.BaseContext;
import com.xlzfa.knowhub.util.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {


    @Autowired
    private AnswerMapper answerMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private LikeRecordMapper likeRecordMapper;

    @Override
    public ResponseResult questionDetail(Long id, Integer pageNum, Integer pageSize) {

        Question question = getById(id);

        QuestionVo questionVo = BeanCopyUtils.copyBean(question, QuestionVo.class);

        questionVo.setUser(userService.getById(question.getUserId()).getUsername());


        long count = answerMapper.selectCount(
                new LambdaQueryWrapper<Answer>()
                        .eq(Answer::getQuestionId, 1)
        );

        questionVo.setAnswerCount(count);

        QuestionDetailVo questionDetailVo = QuestionDetailVo.builder()
                .question(questionVo)
                .answers(AnswerList(id, pageNum, pageSize))
                .build();


        return ResponseResult.success(questionDetailVo);

    }


    public PageVo<AnswerVo> AnswerList(Long id, Integer pageNum, Integer pageSize){

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 50) {
            pageSize = 10;
        }


        Page<Answer> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Answer> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Answer::getStatus, SystemConstants.ANSWER_STATUS_NORMAL)
                .eq(Answer::getQuestionId, id);

        //推荐暂时这么做
        wrapper.orderByDesc(Answer::getIsAccepted); // 已采纳的更值钱
        wrapper.orderByDesc(Answer::getLikeCount);  // 点赞多的优先
        wrapper.orderByDesc(Answer::getCreateTime);// 新回答兜底

        answerMapper.selectPage(page, wrapper);

        List<AnswerVo> vos =
                BeanCopyUtils.copyBeanList(page.getRecords(), AnswerVo.class);


        vos.forEach( vo ->{
            //TODO 后期优化
            User user = userService.getById(vo.getUserId());
            if (user != null){
                vo.setUser(user.getUsername());
                vo.setUserId(user.getId());
            }
            //只返回前三条
            PageVo<CommentVo> commentPage = commentPage(vo.getId(), 1, 3);

            vo.setComments(commentPage);


        });

        vos.forEach( vo ->{
            System.out.println(vo.getComments().getRows().toString());
        });

        PageVo pageVo = new PageVo(vos, page.getTotal());

        return pageVo;

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




    @Override
    public ResponseResult questionInfo(Long id) {

        Question question = getById(id);

        QuestionVo questionVo = BeanCopyUtils.copyBean(question, QuestionVo.class);

        questionVo.setUser(userService.getById(question.getUserId()).getUsername());

        return ResponseResult.success(questionVo);
    }

    @Override
    public ResponseResult updateLike(Long id, boolean like) {

        //TODO 后期先装redis，定时写入mysql

        Long userId = BaseContext.getCurrentId();

        //如果是要点赞
        if (like){

            Long islike = findLikeRecord(userId, id);

            if (islike > 0L){
                likeRecordMapper.deleteById(islike);
            }else {
                LikeRecord newlike = LikeRecord.builder()
                        .userId(userId)
                        .targetType(0)
                        .targetId(id)
                        .build();
                likeRecordMapper.insert(newlike);
            }


        }else {

            Long islike = findLikeRecord(userId, id);

            if (islike == 0L){
                LikeRecord newlike = LikeRecord.builder()
                        .userId(userId)
                        .targetType(0)
                        .targetId(id)
                        .build();
                likeRecordMapper.insert(newlike);
            }else {
                likeRecordMapper.deleteById(islike);
            }


        }

        return ResponseResult.success();

//        if (like){
//            this.lambdaUpdate()
//                    .setSql("like_count = like_count + 1")
//                    .eq(Question::getId, id)
//                    .update();
//        }else {
//            this.lambdaUpdate()
//                    .setSql("like_count = IF(like_count > 0, like_count - 1, 0)")
//                    .eq(Question::getId, id)
//                    .update();
//        }
//
//        LikeVo likeVo = LikeVo.builder()
//                .liked(like)
//                .likeCount(baseMapper.selectById(id).getLikeCount())
//                .build();
//
//        return ResponseResult.success(likeVo);



    }

    public Long findLikeRecord(Long userId, Long questionId){

        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(LikeRecord::getTargetType, 0)
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetId, questionId);

        if (likeRecordMapper.selectOne(wrapper) != null){
            return likeRecordMapper.selectOne(wrapper).getId();
        }else {
            return 0L;
        }



    }








    @Override
    public ResponseResult addQuestion(QuestionAddDto questionAddDto) {


        Question question = Question.builder()
                .userId(questionAddDto.getUserId())
                .title(questionAddDto.getTitle())
                .content(questionAddDto.getContent())
                .status(SystemConstants.QUESTION_STATUS_NORMAL)
                .viewCount(0L)
                .likeCount(0L)
                .answerCount(0L)
                .build();

        baseMapper.insert(question);



        return ResponseResult.success(question.getId());


    }

    @Override
    public ResponseResult myQuestion(Long userId) {

        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Question::getUserId, userId)
                .eq(Question::getStatus, SystemConstants.QUESTION_STATUS_NORMAL)
                .orderByDesc(Question::getCreateTime);

        List<Question> questions = list(wrapper);

        List<QuestionVo> vos = BeanCopyUtils.copyBeanList(questions, QuestionVo.class);

        // 补充 user 和 answerCount
        User user = userService.getById(userId);

        vos.forEach(vo -> {
            if (user != null) {
                vo.setUser(user.getUsername());
                vo.setUserId(user.getId());
            }

            // 装 answerCount（只装数量，不装内容）
            Long count = answerMapper.selectCount(
                    new LambdaQueryWrapper<Answer>()
                            .eq(Answer::getQuestionId, vo.getId())
                            .eq(Answer::getStatus, SystemConstants.ANSWER_STATUS_NORMAL)
            );

            vo.setAnswerCount(count);
        });

        return ResponseResult.success(vos);
    }
}
