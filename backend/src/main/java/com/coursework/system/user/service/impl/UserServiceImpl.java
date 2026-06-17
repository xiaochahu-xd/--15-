package com.coursework.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursework.system.user.dto.UserSummary;
import com.coursework.system.user.entity.Role;
import com.coursework.system.user.entity.User;
import com.coursework.system.user.mapper.UserMapper;
import com.coursework.system.user.service.RoleService;
import com.coursework.system.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final RoleService roleService;

    public UserServiceImpl(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public User getByUsername(String username) {
        return getOne(new QueryWrapper<User>().eq("username", username), false);
    }

    @Override
    public List<UserSummary> listSummaries() {
        List<User> users = list();
        List<UserSummary> summaries = new ArrayList<UserSummary>();
        for (User user : users) {
            UserSummary summary = new UserSummary();
            summary.setId(user.getId());
            summary.setUsername(user.getUsername());
            summary.setRealName(user.getRealName());
            summary.setEmail(user.getEmail());
            summary.setPhone(user.getPhone());
            summary.setStatus(user.getStatus());
            summary.setCreatedAt(user.getCreatedAt());
            List<String> roleCodes = new ArrayList<String>();
            for (Role role : roleService.listByUserId(user.getId())) {
                roleCodes.add(role.getCode());
            }
            summary.setRoles(roleCodes);
            summaries.add(summary);
        }
        return summaries;
    }
}
