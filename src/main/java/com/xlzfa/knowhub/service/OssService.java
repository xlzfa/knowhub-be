package com.xlzfa.knowhub.service;

import com.xlzfa.knowhub.common.ResponseResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public interface OssService {
    ResponseResult uploadAvatar(MultipartFile file);
}
