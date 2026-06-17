package com.coursework.system.auth.service;

import com.coursework.system.auth.dto.AuthResponse;
import com.coursework.system.auth.dto.CurrentUserResponse;
import com.coursework.system.auth.dto.LoginRequest;
import com.coursework.system.common.exception.BusinessException;
import com.coursework.system.common.security.JwtTokenProvider;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.common.utils.RequestUtils;
import com.coursework.system.log.service.OperationLogService;
import com.coursework.system.user.entity.Role;
import com.coursework.system.user.entity.User;
import com.coursework.system.user.service.RoleService;
import com.coursework.system.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {
    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OperationLogService operationLogService;

    public AuthService(UserService userService,
                       RoleService roleService,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       OperationLogService operationLogService) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.operationLogService = operationLogService;
    }

    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        User user = userService.getByUsername(loginRequest.getUsername());
        String ip = RequestUtils.getClientIp(request);
        if (user == null || user.getStatus() == null || user.getStatus() != 1
                || !passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            operationLogService.record(user == null ? null : user.getId(), loginRequest.getUsername(),
                    "LOGIN", "USER", user == null ? null : user.getId(), ip, "FAILED", "用户名或密码错误");
            throw new BusinessException(401, "用户名或密码错误");
        }

        List<String> roleCodes = new ArrayList<String>();
        for (Role role : roleService.listByUserId(user.getId())) {
            roleCodes.add(role.getCode());
        }
        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), user.getPasswordHash(),
                user.getRealName(), user.getStatus(), roleCodes);
        String token = jwtTokenProvider.createToken(principal);
        operationLogService.record(user.getId(), user.getUsername(), "LOGIN", "USER", user.getId(), ip,
                "SUCCESS", "登录成功");
        return new AuthResponse(token, toCurrentUser(principal));
    }

    public CurrentUserResponse toCurrentUser(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(401, "请先登录");
        }
        return new CurrentUserResponse(principal.getId(), principal.getUsername(), principal.getRealName(),
                principal.getRoles());
    }
}
