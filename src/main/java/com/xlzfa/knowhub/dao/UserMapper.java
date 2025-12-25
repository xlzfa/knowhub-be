package com.xlzfa.knowhub.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xlzfa.knowhub.domain.pojo.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper extends BaseMapper<User> {
}
