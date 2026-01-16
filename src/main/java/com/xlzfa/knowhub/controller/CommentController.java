package com.xlzfa.knowhub.controller;


import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.domain.dto.CommentAddDto;
import com.xlzfa.knowhub.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
@Tag( name = "评论模块")
public class CommentController {


    @Autowired
    private CommentService commentService;

    @PostMapping("/add")
    @Operation( summary = "新增评论")
    public ResponseResult add(@RequestBody CommentAddDto commentAddDto){
        return commentService.add(commentAddDto);
    }

    @GetMapping("/list/{answerId}")
    @Operation( summary = "全部评论")
    public ResponseResult list(@PathVariable Long answerId){
        return commentService.list(answerId);
    }


    @GetMapping("/mine")
    @Operation( summary = "我的评论")
    public ResponseResult myComment(@RequestParam Long userId){
        return commentService.myComment(userId);
    }

}
