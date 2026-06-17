package com.coursework.system.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursework.system.user.entity.UserRole;

public interface UserRoleService extends IService<UserRole> {
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
}
