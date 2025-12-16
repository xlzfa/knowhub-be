package com.xlzfa.knowhub.controller;

import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.domain.dto.UserDto;
import com.xlzfa.knowhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Tag(name = "用户模块")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/login")
    @Operation( summary = "用户登录")
    public ResponseResult login(@RequestBody UserDto userDto){
        return userService.login(userDto);
    }

    @GetMapping("/info/{id}")
    @Operation( summary = "用户信息")
    public ResponseResult userInfo(@PathVariable Long id){
        return userService.userInfo(id);
    }

    @PostMapping("/update")
    @Operation( summary = "用户信息更新")
    public ResponseResult updateUser(@RequestBody UserDto userDto) {
        return userService.updateUser(userDto);
    }


}
