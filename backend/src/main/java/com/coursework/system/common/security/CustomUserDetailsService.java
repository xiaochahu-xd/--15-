package com.coursework.system.common.security;

import com.coursework.system.user.entity.Role;
import com.coursework.system.user.entity.User;
import com.coursework.system.user.service.RoleService;
import com.coursework.system.user.service.UserService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;
    private final RoleService roleService;

    public CustomUserDetailsService(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @Override
    public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return buildPrincipal(user);
    }

    public UserPrincipal loadUserPrincipalById(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return buildPrincipal(user);
    }

    private UserPrincipal buildPrincipal(User user) {
        List<Role> roleList = roleService.listByUserId(user.getId());
        List<String> roleCodes = new ArrayList<String>();
        for (Role role : roleList) {
            roleCodes.add(role.getCode());
        }
        return new UserPrincipal(user.getId(), user.getUsername(), user.getPasswordHash(),
                user.getRealName(), user.getStatus(), roleCodes);
    }
}
