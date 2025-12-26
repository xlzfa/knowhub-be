package com.xlzfa.knowhub.controller;


import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.domain.dto.LikeDto;
import com.xlzfa.knowhub.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/question")
@Tag( name = "问题模块")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/detail")
    @Operation( summary = "问题详情")
    public ResponseResult questionDetail(@RequestParam Long id, @RequestParam Integer pageNum, @RequestParam Integer pageSize){
        return questionService.questionDetail(id,pageNum,pageSize);
    }

    @GetMapping("/{id}")
    @Operation( summary = "问题信息")
    public ResponseResult questionInfo(@PathVariable Long id){
        return questionService.questionInfo(id);
    }

    @PostMapping("/like")
    @Operation( summary = "点赞功能")
    public ResponseResult answerLike(@RequestBody LikeDto dto){
        return questionService.updateLike(dto.getId(), dto.getLike());
    }
}
