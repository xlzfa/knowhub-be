package com.xlzfa.knowhub.domain.vo;

import com.xlzfa.knowhub.domain.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVo {



    private Long id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论用户信息
     */
    private String username;

    private Long userId;

    /**
     * 关联的 answerId
     */
    private Long answerId;

    /**
     * 父评论 id
     * 根评论固定为 -1
     */
    private Long parentId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
