package com.xlzfa.knowhub.controller;


import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("upload")
@Tag( name = "上传模块" )
public class UploadController {

    @Autowired
    private OssService ossService;

    @PostMapping("/avatar")
    @Operation( summary = "上传头像")
    public ResponseResult uploadAvatar(@RequestParam("file") MultipartFile file){

        return ossService.uploadAvatar(file);

    }




}
