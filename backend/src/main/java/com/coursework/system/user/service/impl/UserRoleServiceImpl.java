package com.coursework.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursework.system.user.entity.UserRole;
import com.coursework.system.user.mapper.UserRoleMapper;
import com.coursework.system.user.service.UserRoleService;
import org.springframework.stereotype.Service;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    @Override
    public boolean existsByUserIdAndRoleId(Long userId, Long roleId) {
        return count(new QueryWrapper<UserRole>().eq("user_id", userId).eq("role_id", roleId)) > 0;
    }
}
