package com.xlzfa.knowhub.domain.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.io.Serializable;

/**
 * (LikeRecord)实体类
 *
 * @author makejava
 * @since 2025-12-13 16:24:29
 */

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LikeRecord implements Serializable {
    private static final long serialVersionUID = -74294123330935029L;

    private Long id;

    private Long userId;

    private Long targetId;

    private Integer targetType;

    private Date createTime;

    private Date updateTime;


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

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Integer getTargetType() {
        return targetType;
    }

    public void setTargetType(Integer targetType) {
        this.targetType = targetType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}

