package com.xlzfa.knowhub.controller;


import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.service.AnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/answer")
@Tag( name = "回答模块")
public class AnswerController {

    @Autowired
    private AnswerService answerService;


    @GetMapping("/feed")
    @Operation( summary = "推荐流")
    public ResponseResult answerFeed(@RequestParam Integer pageNum, @RequestParam Integer pageSize){
        return answerService.answerFeed(pageNum, pageSize);
    }


}
