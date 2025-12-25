package com.xlzfa.knowhub.domain.pojo;

import java.util.Date;
import java.io.Serializable;

/**
 * (Comment)实体类
 *
 * @author makejava
 * @since 2025-12-13 16:24:46
 */
public class Comment implements Serializable {
    private static final long serialVersionUID = -61252209888740352L;

    private Long id;

    private Long userId;

    private Long answerId;

    private Long parentId;

    private String content;

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

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}

