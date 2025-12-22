package com.xlzfa.knowhub.controller;


import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.domain.dto.LikeDto;
import com.xlzfa.knowhub.service.AnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/like")
    @Operation( summary = "点赞功能")
    public ResponseResult answerLike(@RequestBody LikeDto dto){
        return answerService.updateLike(dto.getId(), dto.getLike());
    }

}
