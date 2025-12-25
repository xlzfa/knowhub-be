package com.xlzfa.knowhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xlzfa.knowhub.common.ResponseResult;
import com.xlzfa.knowhub.config.JwtProperties;
import com.xlzfa.knowhub.dao.UserMapper;
import com.xlzfa.knowhub.domain.dto.UserDto;
import com.xlzfa.knowhub.domain.pojo.User;
import com.xlzfa.knowhub.domain.vo.UserInfoVo;
import com.xlzfa.knowhub.domain.vo.UserLoginVo;
import com.xlzfa.knowhub.service.UserService;
import com.xlzfa.knowhub.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public ResponseResult login(UserDto userDto) {

        //查user
        String username = userDto.getUsername();
        String password = userDto.getPassword();

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
                .eq(User::getPassword, password);

        User user = getOne(wrapper, false);

        if (user == null){
            return ResponseResult.error("账号或密码错误");
        }

        //装配载荷
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        //建token
        String token = JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(), claims);

        //装配
        UserLoginVo userLoginVo = UserLoginVo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .bio(user.getBio())
                .token(token)
                .build();

        return ResponseResult.success(userLoginVo);
    }

    @Override
    public ResponseResult userInfo(Long id) {

        User user = getById(id);
        UserInfoVo userInfoVo = UserInfoVo.builder()
                .id(id)
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .email(user.getEmail())
                .createTime(user.getCreateTime())
                .build();


        return ResponseResult.success(userInfoVo);
    }

    @Override
    public ResponseResult updateUser(UserDto userDto) {

//        System.out.println(userDto.toString());

        User user = getById(userDto.getId());

        if (StringUtils.hasText(userDto.getUsername())) {
            user.setUsername(userDto.getUsername());
        }

//        System.out.println("---------" + userDto.getUsername() + user.getUsername());

        if (StringUtils.hasText(userDto.getPassword())) {
            user.setPassword(userDto.getPassword());
        }

        if (StringUtils.hasText(userDto.getEmail())) {
            user.setEmail(userDto.getEmail());
        }

        if (StringUtils.hasText(userDto.getAvatar())) {
            user.setAvatar(userDto.getAvatar());
        }

        if (StringUtils.hasText(userDto.getBio())) {
            user.setBio(userDto.getBio());
        }

        updateById(user);

        UserInfoVo userInfoVo = UserInfoVo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .email(user.getEmail())
                .createTime(user.getCreateTime())
                .build();

        return ResponseResult.success(userInfoVo);
    }
}















