package com.coursework.system.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursework.system.user.entity.Role;

import java.util.List;

public interface RoleService extends IService<Role> {
    Role getByCode(String code);

    List<Role> listByUserId(Long userId);
}
