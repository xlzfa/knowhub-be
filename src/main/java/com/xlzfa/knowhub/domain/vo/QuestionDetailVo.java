package com.xlzfa.knowhub.domain.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDetailVo {


    private QuestionVo question;


    private PageVo<AnswerVo> answers;


}
