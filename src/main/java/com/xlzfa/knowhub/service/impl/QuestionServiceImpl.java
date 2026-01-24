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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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


        boolean liked = likeRecordMapper.exists(
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getUserId, BaseContext.getCurrentId())
                        .eq(LikeRecord::getTargetType, 0)
                        .eq(LikeRecord::getTargetId, id)
        );

        questionVo.setLiked(liked);


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

        //优化：解决N+1

        //查用户名
        Set<Long> userIds = vos.stream()
                .map(AnswerVo::getUserId)
                .collect(Collectors.toSet());

        List<User> users = userService.listByIds(userIds);

        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        //查是否点赞过
        List<LikeRecord> likeRecords = likeRecordMapper.selectList(
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getUserId, BaseContext.getCurrentId())
                        .eq(LikeRecord::getTargetType, 1)
        );

        Set<Long> likedAnswerIds = likeRecords.stream()
                .map(LikeRecord::getTargetId)
                .collect(Collectors.toSet());

        //查所有评论

        List<Long> answerIds = vos.stream()
                .map(AnswerVo::getId)
                .collect(Collectors.toList());

        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .in(Comment::getAnswerId, answerIds)
                        .orderByDesc(Comment::getCreateTime)
        );

        Set<Long> commentUserIds = comments.stream()
                .map(Comment::getUserId)
                .collect(Collectors.toSet());

        List<User> commentUsers = userService.listByIds(commentUserIds);

        Map<Long, User> commentUserMap = commentUsers.stream()
                .collect(Collectors.toMap(User::getId, u -> u));



        Map<Long, List<CommentVo>> commentMap = comments.stream()
                .map(c -> {
                    CommentVo vo = BeanCopyUtils.copyBean(c, CommentVo.class);
                    User user = commentUserMap.get(c.getUserId());
                    vo.setUsername(user != null ? user.getUsername() : "匿名");
                    return vo;
                })
                .collect(Collectors.groupingBy(
                        CommentVo::getAnswerId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream().limit(3).collect(Collectors.toList())
                        )
                ));




        //遍历装载
        for (AnswerVo vo : vos){

            User user = userMap.get(vo.getUserId());
            vo.setUser(user != null ? user.getUsername() : "匿名");
            vo.setLiked(likedAnswerIds.contains(vo.getId()));

            List<CommentVo> commentVos = commentMap.getOrDefault(vo.getId(), Collections.emptyList());

            vo.setComments(new PageVo<>(
                    commentVos,
                    Long.valueOf(commentVos.size())));

            //只返回前三条
//            PageVo<CommentVo> commentPage = commentPage(vo.getId(), 1, 3);
//
//            vo.setComments(commentPage);
        }


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



        //优化：解决N+1

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

        boolean res = true;

        //如果是要点赞
        if (like){

            Long islike = findLikeRecord(userId, id);



            if (islike > 0L){

                this.lambdaUpdate()
                    .setSql("like_count = IF(like_count > 0, like_count - 1, 0)")
                    .eq(Question::getId, id)
                    .update();

                likeRecordMapper.deleteById(islike);

                res = false;
            }else {
                this.lambdaUpdate()
                    .setSql("like_count = like_count + 1")
                    .eq(Question::getId, id)
                    .update();

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

                this.lambdaUpdate()
                        .setSql("like_count = like_count + 1")
                        .eq(Question::getId, id)
                        .update();

                LikeRecord newlike = LikeRecord.builder()
                        .userId(userId)
                        .targetType(0)
                        .targetId(id)
                        .build();

                likeRecordMapper.insert(newlike);
            }else {

                this.lambdaUpdate()
                        .setSql("like_count = IF(like_count > 0, like_count - 1, 0)")
                        .eq(Question::getId, id)
                        .update();

                likeRecordMapper.deleteById(islike);

                res = false;
            }


        }

        return ResponseResult.success(res);


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

        // 补充 user
        User user = userService.getById(userId);



        vos.forEach(vo -> {

            if (user != null) {
                vo.setUser(user.getUsername());
                vo.setUserId(user.getId());
            }

        });

        return ResponseResult.success(vos);
    }
}
