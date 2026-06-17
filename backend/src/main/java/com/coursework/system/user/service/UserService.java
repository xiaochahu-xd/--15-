package com.coursework.system.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursework.system.user.dto.UserSummary;
import com.coursework.system.user.entity.User;

import java.util.List;

public interface UserService extends IService<User> {
    User getByUsername(String username);

    List<UserSummary> listSummaries();
}
