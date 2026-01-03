package com.xlzfa.knowhub.service.impl;

import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.service.OssService;
import com.xlzfa.knowhub.util.OssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;


@Service
public class OssServiceImpl implements OssService {

    @Autowired
    private OssUtil ossUtil;


    @Override
    public ResponseResult uploadAvatar(MultipartFile file) {
        return ResponseResult.success(ossUtil.upload(file,"avatar"));
    }

    @Override
    public ResponseResult uploadAnswerImg(MultipartFile file) {

        return ResponseResult.success();



    }
}
