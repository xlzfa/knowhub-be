package com.xlzfa.knowhub.controller;

import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.domain.dto.UserDto;
import com.xlzfa.knowhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/login")
    public ResponseResult login(@RequestBody UserDto userDto){
        return userService.login(userDto);
    }



}
