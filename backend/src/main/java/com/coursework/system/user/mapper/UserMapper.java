package com.coursework.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursework.system.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
