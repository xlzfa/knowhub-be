package com.xlzfa.knowhub.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionVo {

    private Long id;

    private Long answerCount;

    private String title;

    private Integer status;

    private Long userId;

    private String user;

    private String content;

    private Integer likeCount;

    private Integer viewCount;

    private Date createTime;


}
