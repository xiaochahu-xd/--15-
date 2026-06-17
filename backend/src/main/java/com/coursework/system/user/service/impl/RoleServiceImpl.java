package com.coursework.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursework.system.user.entity.Role;
import com.coursework.system.user.mapper.RoleMapper;
import com.coursework.system.user.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public Role getByCode(String code) {
        return getOne(new QueryWrapper<Role>().eq("code", code), false);
    }

    @Override
    public List<Role> listByUserId(Long userId) {
        return baseMapper.selectRolesByUserId(userId);
    }
}
