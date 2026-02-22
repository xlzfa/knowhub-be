package com.xlzfa.knowhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.common.SystemConstants;
import com.xlzfa.knowhub.config.RabbitConfig;
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
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;


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
    @Autowired
    private DefaultRedisScript<List> likeLuaScript;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private AmqpTemplate amqpTemplate;




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


        //拿id列表
        List<Long> answerIds = vos.stream()
                .map(AnswerVo::getId)
                .collect(Collectors.toList());


        //查是否点赞过
        List<LikeRecord> likeRecords = likeRecordMapper.selectList(
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getUserId, BaseContext.getCurrentId())
                        .eq(LikeRecord::getTargetType, 1)
        );

        Set<Long> likedAnswerIds = likeRecords.stream()
                .map(LikeRecord::getTargetId)
                .collect(Collectors.toSet());


        //在redis拿点赞数
        List<String> keys = answerIds.stream()
                .map(id -> "answer:like:count:" + id)
                .collect(Collectors.toList());


        List<Object> likeCounts = redisTemplate.opsForValue().multiGet(keys);

        HashMap<Long, Long> likeCountMap = new HashMap<>();

        for (int i = 0; i < answerIds.size(); i++){
            Object countObj = likeCounts.get(i);
            Long count = countObj == null ? 0L : Long.parseLong(countObj.toString());
            likeCountMap.put(answerIds.get(i),count);
        }
        //查所有评论


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

            vo.setLikeCount(likeCountMap.get(vo.getId()));

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

        List<Long> result = stringRedisTemplate.execute(
                likeLuaScript,
                Arrays.asList(userKey, countKey, dirtyKey),
                userId.toString(),
                id.toString(),
                like ? "1" : "0"
        );

        Integer liked = result.get(0).intValue();
        Long likeCount = result.get(1);

        LikeVo likeVo = new LikeVo();
        likeVo.setLiked(liked == 1);
        likeVo.setLikeCount(likeCount);


        HashMap<String, Object> msg = new HashMap<>();
        msg.put("userId",userId);
        msg.put("targetId",id);
        msg.put("liked",liked);

        amqpTemplate.convertAndSend(RabbitConfig.LIKE_EXCHANGE,RabbitConfig.ANSWER_ROUTING_KEY,msg);


        return ResponseResult.success(likeVo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeSql(Long userId, Long id, Integer liked) {

        if(liked == 1){
            LikeRecord build = LikeRecord.builder()
                    .userId(userId)
                    .targetId(id)
                    .targetType(1)
                    .build();
            likeRecordMapper.insert(build);
            answerMapper.update(
                    null,
                    new UpdateWrapper<Answer>()
                            .setSql("like_count = like_count + 1")
                            .eq("id", id)
            );
        }else {
            likeRecordMapper.delete(
                    new LambdaQueryWrapper<LikeRecord>()
                            .eq(LikeRecord::getUserId, userId)
                            .eq(LikeRecord::getTargetId, id)
                            .eq(LikeRecord::getTargetType, 1)
            );
            answerMapper.update(
                    null,
                    new UpdateWrapper<Answer>()
                            .setSql("like_count = IF(like_count > 0, like_count - 1, 0)")
                            .eq("id", id)
            );
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
