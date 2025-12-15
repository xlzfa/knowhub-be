package com.xlzfa.knowhub.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.domain.dto.UserDto;
import com.xlzfa.knowhub.domain.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService extends IService<User> {


    ResponseResult login(UserDto userDto);

    ResponseResult userInfo(Long id);
}
