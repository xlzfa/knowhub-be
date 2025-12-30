package com.xlzfa.knowhub.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAddDto {


    private Long userId;

    private String title;

    private String content;



}
