package com.xlzfa.knowhub.controller;


import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.domain.dto.CommentAddDto;
import com.xlzfa.knowhub.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
