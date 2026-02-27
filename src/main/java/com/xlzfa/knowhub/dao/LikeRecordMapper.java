package com.xlzfa.knowhub.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xlzfa.knowhub.domain.pojo.LikeRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {



    @Insert("INSERT ignore INTO like_record(user_id, target_id, target_type) VALUES (#{userId}, #{targetId}, #{targetType})")
    int insertIgnore(@Param("userId") Long userId,
                     @Param("targetId") Long targetId,
                     @Param("targetType") Integer targetType);


}
