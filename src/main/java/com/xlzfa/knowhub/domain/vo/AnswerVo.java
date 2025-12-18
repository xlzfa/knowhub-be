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
public class AnswerVo {

    private Long id;

    private Long questionId;

    private String quertionTitle;

    private Integer status;

    private Long userId;

    private String content;

    private Integer likeCount;

    private Integer isAccepted;

    private Date createTime;

}
