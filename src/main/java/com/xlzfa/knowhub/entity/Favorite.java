package com.xlzfa.knowhub.entity;

import java.util.Date;
import java.io.Serializable;

/**
 * (Favorite)实体类
 *
 * @author makejava
 * @since 2025-12-13 16:24:38
 */
public class Favorite implements Serializable {
    private static final long serialVersionUID = -41379077538437415L;

    private Long id;

    private Long userId;

    private Long questionId;

    private Date createTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}

