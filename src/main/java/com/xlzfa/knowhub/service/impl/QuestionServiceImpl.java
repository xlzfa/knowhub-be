package com.xlzfa.knowhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.common.SystemConstants;
import com.xlzfa.knowhub.dao.AnswerMapper;
import com.xlzfa.knowhub.dao.QuestionMapper;
import com.xlzfa.knowhub.domain.entity.Answer;
import com.xlzfa.knowhub.domain.entity.Question;
import com.xlzfa.knowhub.domain.entity.User;
import com.xlzfa.knowhub.domain.vo.AnswerVo;
import com.xlzfa.knowhub.domain.vo.PageVo;
import com.xlzfa.knowhub.domain.vo.QuestionVo;
import com.xlzfa.knowhub.service.QuestionService;
import com.xlzfa.knowhub.service.UserService;
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

    @Override
    public ResponseResult questionDetail(Long id, Integer pageNum, Integer pageSize) {

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

        });

        PageVo pageVo = new PageVo(vos, page.getTotal());

        return ResponseResult.success(pageVo);

    }

    @Override
    public ResponseResult questionInfo(Long id) {

        Question question = getById(id);

        QuestionVo questionVo = BeanCopyUtils.copyBean(question, QuestionVo.class);

        questionVo.setUser(userService.getById(id).getUsername());

        return ResponseResult.success(questionVo);
    }
}
