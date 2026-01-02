package com.xlzfa.knowhub.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerAddDto {

    private Long questionId;
    private Long userId;
    private String content;



}
