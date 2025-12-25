package com.xlzfa.knowhub.domain.pojo;

import java.io.Serializable;

/**
 * (QuestionTag)实体类
 *
 * @author makejava
 * @since 2025-12-13 16:24:11
 */
public class QuestionTag implements Serializable {
    private static final long serialVersionUID = -81734122124315701L;

    private Long id;

    private Long questionId;

    private Integer tagId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

}

