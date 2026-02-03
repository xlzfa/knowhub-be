package com.xlzfa.knowhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;


import java.util.*;
import java.util.stream.Collectors;


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
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Value("${pressure.enabled:false}")
    private boolean pressureEnabled;

    private boolean isPressureTest() {
        return pressureEnabled;
    }


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

        //优化：解决N+1

        //查问题标题
        Set<Long> questionIds = vos.stream()
                .map(AnswerVo::getQuestionId)
                .collect(Collectors.toSet());

        List<Question> questions = questionService.listByIds(questionIds);

        Map<Long, String> questionTitlesMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q.getTitle()));

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

        List<Map<String, Object>> commentCountList =
                commentMapper.selectMaps(
                        new QueryWrapper<Comment>()
                                .select("answer_id, count(*) as cnt")
                                .in("answer_id", answerIds)
                                .groupBy("answer_id")
                );

        Map<Long, Long> commentCountMap = commentCountList.stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("answer_id")).longValue(),
                        m -> ((Number) m.get("cnt")).longValue()
                ));


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
        for (AnswerVo vo : vos) {

            User user = userMap.get(vo.getUserId());

            vo.setUser(user != null ? user.getUsername() : "匿名");

            vo.setLiked(likedAnswerIds.contains(vo.getId()));

            vo.setQuertionTitle(questionTitlesMap.get(vo.getQuestionId()));

            List<CommentVo> commentVos = commentMap.getOrDefault(vo.getId(), Collections.emptyList());

            vo.setComments(new PageVo<>(
                    commentVos,
                    commentCountMap.getOrDefault(vo.getId(), 0L)));

        }

        PageVo pageVo = new PageVo(vos, page.getTotal());

        return ResponseResult.success(pageVo);

    }


    @Override
    public ResponseResult updateLike(Long id, boolean like) {

        Long userId = BaseContext.getCurrentId();

        // 压测模式
        if (id != null && isPressureTest()) {
            userId = id;
        }

        String userKey = "answer:like:users:" + id;
        String countKey = "answer:like:count:" + id;
        String dirtyKey = "answer:like:dirty";

        List<Long> result = redisTemplate.execute(
                likeLuaScript,
                Arrays.asList(userKey, countKey, dirtyKey),
                userId.toString(),
                id.toString()
        );

        Integer liked = result.get(0).intValue();
        Long likeCount = result.get(1);

        LikeVo likeVo = new LikeVo();
        likeVo.setLiked(liked == 1);
        likeVo.setLikeCount(likeCount);

        return ResponseResult.success(likeVo);
    }




//    @Override
//    public ResponseResult updateLike(Long id, boolean like) {
//
//
//        Long userId = BaseContext.getCurrentId();
//
//
//
//        // 压测模式下，允许 body 覆盖 userId
//        if (id != null && isPressureTest()) {
//            userId = id;
//        }
//
//        LikeVo likeVo = new LikeVo();
//
//        likeVo.setLiked(true);
//
//        Answer answer = baseMapper.selectById(id);
//
//        Long likeCount = answer.getLikeCount();
//
//        //如果是要点赞
//        if (like){
//
//            Long islike = findLikeRecord(userId, id);
//
//            if (islike > 0L){
//
//                this.lambdaUpdate()
//                        .setSql("like_count = IF(like_count > 0, like_count - 1, 0)")
//                        .eq(Answer::getId, id)
//                        .update();
//
//                likeCount--;
//
//                likeRecordMapper.deleteById(islike);
//                likeVo.setLiked(false);
//            }else {
//                this.lambdaUpdate()
//                        .setSql("like_count = like_count + 1")
//                        .eq(Answer::getId, id)
//                        .update();
//
//                LikeRecord newlike = LikeRecord.builder()
//                        .userId(userId)
//                        .targetType(1)
//                        .targetId(id)
//                        .build();
//
//                likeCount++;
//
//                likeRecordMapper.insert(newlike);
//            }
//
//
//        }else {
//
//            Long islike = findLikeRecord(userId, id);
//
//            if (islike == 0L){
//
//                this.lambdaUpdate()
//                        .setSql("like_count = like_count + 1")
//                        .eq(Answer::getId, id)
//                        .update();
//
//                LikeRecord newlike = LikeRecord.builder()
//                        .userId(userId)
//                        .targetType(1)
//                        .targetId(id)
//                        .build();
//
//                likeCount++;
//
//                likeRecordMapper.insert(newlike);
//            }else {
//
//                this.lambdaUpdate()
//                        .setSql("like_count = IF(like_count > 0, like_count - 1, 0)")
//                        .eq(Answer::getId, id)
//                        .update();
//
//                likeCount--;
//
//                likeRecordMapper.deleteById(islike);
//                likeVo.setLiked(false);
//            }
//
//
//        }
//
//        likeVo.setLikeCount(likeCount);
//
//        return ResponseResult.success(likeVo);
//
//
//    }
//
//    public Long findLikeRecord(Long userId, Long answerId){
//
//        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
//
//        wrapper.eq(LikeRecord::getTargetType, 1)
//                .eq(LikeRecord::getUserId, userId)
//                .eq(LikeRecord::getTargetId, answerId);
//
//        if (likeRecordMapper.selectOne(wrapper) != null){
//            return likeRecordMapper.selectOne(wrapper).getId();
//        }else {
//            return 0L;
//        }
//
//
//
//    }

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

        questionService.lambdaUpdate()
                .setSql("answer_count = answer_count + 1")
                .eq(Question::getId, answer.getQuestionId())
                .update();

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

        //查问题标题
        Set<Long> questionIds = vos.stream()
                .map(AnswerVo::getQuestionId)
                .collect(Collectors.toSet());

        List<Question> questions = questionService.listByIds(questionIds);

        Map<Long, String> questionTitlesMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q.getTitle()));



        User user = userService.getById(userId);

        vos.forEach( vo ->{

            vo.setQuertionTitle(questionTitlesMap.get(vo.getQuestionId()));

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





}
