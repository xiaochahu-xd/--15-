package com.coursework.system.auth.service;

import com.coursework.system.auth.dto.AuthResponse;
import com.coursework.system.auth.dto.CurrentUserResponse;
import com.coursework.system.auth.dto.LoginRequest;
import com.coursework.system.auth.dto.RegisterRequest;
import com.coursework.system.common.exception.BusinessException;
import com.coursework.system.common.security.JwtTokenProvider;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.common.utils.RequestUtils;
import com.coursework.system.log.service.OperationLogService;
import com.coursework.system.user.entity.Role;
import com.coursework.system.user.entity.User;
import com.coursework.system.user.entity.UserRole;
import com.coursework.system.user.service.RoleService;
import com.coursework.system.user.service.UserRoleService;
import com.coursework.system.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {
    private final UserService userService;
    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OperationLogService operationLogService;

    public AuthService(UserService userService,
                       RoleService roleService,
                       UserRoleService userRoleService,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       OperationLogService operationLogService) {
        this.userService = userService;
        this.roleService = roleService;
        this.userRoleService = userRoleService;
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

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest, HttpServletRequest request) {
        String username = registerRequest.getUsername().trim();
        String roleCode = registerRequest.getRoleCode().trim().toUpperCase();
        String ip = RequestUtils.getClientIp(request);
        if (!"TEACHER".equals(roleCode) && !"STUDENT".equals(roleCode)) {
            throw new BusinessException(400, "只能注册为教师或学生");
        }
        if (userService.getByUsername(username) != null) {
            throw new BusinessException(400, "用户名已存在");
        }
        Role role = roleService.getByCode(roleCode);
        if (role == null) {
            throw new BusinessException(400, "注册角色不存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRealName(registerRequest.getRealName().trim());
        user.setEmail(trimToNull(registerRequest.getEmail()));
        user.setPhone(trimToNull(registerRequest.getPhone()));
        user.setStatus(1);
        userService.save(user);

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        userRoleService.save(userRole);

        List<String> roleCodes = new ArrayList<String>();
        roleCodes.add(role.getCode());
        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), user.getPasswordHash(),
                user.getRealName(), user.getStatus(), roleCodes);
        String token = jwtTokenProvider.createToken(principal);
        operationLogService.record(user.getId(), user.getUsername(), "REGISTER", "USER", user.getId(), ip,
                "SUCCESS", "用户自助注册为" + role.getName());
        return new AuthResponse(token, toCurrentUser(principal));
    }

    public CurrentUserResponse toCurrentUser(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(401, "请先登录");
        }
        return new CurrentUserResponse(principal.getId(), principal.getUsername(), principal.getRealName(),
                principal.getRoles());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
