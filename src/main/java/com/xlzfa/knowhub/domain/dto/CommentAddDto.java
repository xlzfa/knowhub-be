package com.xlzfa.knowhub.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentAddDto {

    private Long userId;

    private Long answerId;

    private Long parentId;

    private String content;



}
