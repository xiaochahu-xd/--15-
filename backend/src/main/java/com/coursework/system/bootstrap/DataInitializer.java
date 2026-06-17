package com.coursework.system.bootstrap;

import com.coursework.system.user.entity.Role;
import com.coursework.system.user.entity.User;
import com.coursework.system.user.entity.UserRole;
import com.coursework.system.user.service.RoleService;
import com.coursework.system.user.service.UserRoleService;
import com.coursework.system.user.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {
    private static final String DEFAULT_PASSWORD = "123456";

    private final UserService userService;
    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserService userService,
                           RoleService roleService,
                           UserRoleService userRoleService,
                           PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.userRoleService = userRoleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role adminRole = ensureRole("ADMIN", "管理员", "系统最高管理权限，可审批课程和查看日志");
        Role teacherRole = ensureRole("TEACHER", "教师", "课程申请、课程成员管理和作业管理");
        Role assistantRole = ensureRole("ASSISTANT", "助教", "协助教师批改作业和查看负责课程");
        Role studentRole = ensureRole("STUDENT", "学生", "查看课程、提交作业和查看成绩");

        ensureUser("admin", "系统管理员", "admin@example.com", adminRole);
        ensureUser("teacher01", "教师一号", "teacher01@example.com", teacherRole);
        ensureUser("assistant01", "助教一号", "assistant01@example.com", assistantRole);
        ensureUser("student01", "学生一号", "student01@example.com", studentRole);
        ensureUser("student02", "学生二号", "student02@example.com", studentRole);
        ensureUser("student03", "学生三号", "student03@example.com", studentRole);
    }

    private Role ensureRole(String code, String name, String description) {
        Role role = roleService.getByCode(code);
        if (role != null) {
            return role;
        }
        Role created = new Role();
        created.setCode(code);
        created.setName(name);
        created.setDescription(description);
        roleService.save(created);
        return created;
    }

    private void ensureUser(String username, String realName, String email, Role role) {
        User user = userService.getByUsername(username);
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            user.setRealName(realName);
            user.setEmail(email);
            user.setStatus(1);
            userService.save(user);
        }
        if (!userRoleService.existsByUserIdAndRoleId(user.getId(), role.getId())) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());
            userRoleService.save(userRole);
        }
    }
}
