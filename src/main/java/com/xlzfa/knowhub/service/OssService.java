package com.xlzfa.knowhub.service;

import com.xlzfa.knowhub.common.ResponseResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


public interface OssService {
    ResponseResult uploadAvatar(MultipartFile file);

    ResponseResult uploadAnswerImg(MultipartFile file);
}
