package com.coursework.system.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursework.system.common.exception.BusinessException;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.course.dto.CourseMemberAddRequest;
import com.coursework.system.course.dto.CourseMemberSummary;
import com.coursework.system.course.dto.CourseSummary;
import com.coursework.system.course.entity.Course;
import com.coursework.system.course.entity.CourseMember;
import com.coursework.system.course.mapper.CourseMapper;
import com.coursework.system.course.service.CourseMemberService;
import com.coursework.system.course.service.CourseService;
import com.coursework.system.log.service.OperationLogService;
import com.coursework.system.user.entity.Role;
import com.coursework.system.user.entity.User;
import com.coursework.system.user.service.RoleService;
import com.coursework.system.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    private static final Set<String> ALLOWED_MEMBER_ROLES =
            new HashSet<String>(Arrays.asList("TEACHER", "ASSISTANT", "STUDENT"));

    private final CourseMemberService courseMemberService;
    private final UserService userService;
    private final RoleService roleService;
    private final OperationLogService operationLogService;

    public CourseServiceImpl(CourseMemberService courseMemberService,
                             UserService userService,
                             RoleService roleService,
                             OperationLogService operationLogService) {
        this.courseMemberService = courseMemberService;
        this.userService = userService;
        this.roleService = roleService;
        this.operationLogService = operationLogService;
    }

    @Override
    public Course getByCourseCode(String courseCode) {
        return getOne(new QueryWrapper<Course>().eq("course_code", courseCode), false);
    }

    @Override
    public List<CourseSummary> listMyCourses(UserPrincipal principal) {
        ensureLogin(principal);
        if (isAdmin(principal)) {
            return listAllCourses();
        }
        return baseMapper.selectSummariesForUser(principal.getId());
    }

    @Override
    public List<CourseSummary> listAllCourses() {
        return baseMapper.selectAllSummaries();
    }

    @Override
    public List<CourseMemberSummary> listMembers(Long courseId, UserPrincipal principal) {
        ensureLogin(principal);
        if (!canViewCourse(courseId, principal)) {
            throw new BusinessException(403, "无权查看该课程成员");
        }
        return courseMemberService.listActiveMembers(courseId);
    }

    @Override
    @Transactional
    public CourseMemberSummary addMember(Long courseId, CourseMemberAddRequest request, UserPrincipal principal, String ip) {
        ensureLogin(principal);
        if (!canManageCourse(courseId, principal)) {
            recordDenied(principal, ip, "COURSE_MEMBER_ADD", courseId, "无权添加课程成员");
            throw new BusinessException(403, "只有课程负责人或管理员可以管理成员");
        }
        Course course = getById(courseId);
        if (course == null || !"ACTIVE".equals(course.getStatus())) {
            throw new BusinessException(404, "课程不存在或不可用");
        }
        String memberRole = request.getMemberRole() == null ? "" : request.getMemberRole().trim().toUpperCase();
        if (!ALLOWED_MEMBER_ROLES.contains(memberRole)) {
            throw new BusinessException(400, "成员角色只能是 TEACHER、ASSISTANT 或 STUDENT");
        }
        if (course.getOwnerId().equals(request.getUserId())) {
            throw new BusinessException(400, "课程负责人已在成员列表中");
        }
        User targetUser = userService.getById(request.getUserId());
        if (targetUser == null || targetUser.getStatus() == null || targetUser.getStatus() != 1) {
            throw new BusinessException(404, "目标用户不存在或已禁用");
        }
        if (!userHasRole(targetUser.getId(), memberRole)) {
            throw new BusinessException(400, "目标用户不具备对应系统角色：" + memberRole);
        }

        CourseMember member = courseMemberService.getByCourseIdAndUserId(courseId, targetUser.getId());
        if (member == null) {
            member = new CourseMember();
            member.setCourseId(courseId);
            member.setUserId(targetUser.getId());
            member.setMemberRole(memberRole);
            member.setJoinedAt(LocalDateTime.now());
            member.setStatus(1);
            courseMemberService.save(member);
        } else {
            member.setMemberRole(memberRole);
            member.setJoinedAt(LocalDateTime.now());
            member.setStatus(1);
            courseMemberService.updateById(member);
        }

        operationLogService.record(principal.getId(), principal.getUsername(), "COURSE_MEMBER_ADD",
                "COURSE", courseId, ip, "SUCCESS", "添加成员：" + targetUser.getUsername() + "，角色：" + memberRole);
        return toMemberSummary(member, targetUser);
    }

    @Override
    @Transactional
    public void removeMember(Long courseId, Long userId, UserPrincipal principal, String ip) {
        ensureLogin(principal);
        if (!canManageCourse(courseId, principal)) {
            recordDenied(principal, ip, "COURSE_MEMBER_REMOVE", courseId, "无权移除课程成员");
            throw new BusinessException(403, "只有课程负责人或管理员可以管理成员");
        }
        Course course = getById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        if (course.getOwnerId().equals(userId)) {
            throw new BusinessException(400, "不能移除课程负责人");
        }
        CourseMember member = courseMemberService.getByCourseIdAndUserId(courseId, userId);
        if (member == null || member.getStatus() == null || member.getStatus() != 1) {
            throw new BusinessException(404, "课程成员不存在");
        }
        member.setStatus(0);
        courseMemberService.updateById(member);
        operationLogService.record(principal.getId(), principal.getUsername(), "COURSE_MEMBER_REMOVE",
                "COURSE", courseId, ip, "SUCCESS", "移除成员用户ID：" + userId);
    }

    @Override
    public boolean canManageCourse(Long courseId, UserPrincipal principal) {
        if (principal == null) {
            return false;
        }
        if (isAdmin(principal)) {
            return true;
        }
        Course course = getById(courseId);
        return course != null && course.getOwnerId().equals(principal.getId()) && hasRole(principal, "TEACHER");
    }

    @Override
    public boolean canViewCourse(Long courseId, UserPrincipal principal) {
        if (principal == null) {
            return false;
        }
        if (isAdmin(principal)) {
            return true;
        }
        Course course = getById(courseId);
        if (course == null || !"ACTIVE".equals(course.getStatus())) {
            return false;
        }
        return course.getOwnerId().equals(principal.getId())
                || courseMemberService.isActiveMember(courseId, principal.getId());
    }

    private CourseMemberSummary toMemberSummary(CourseMember member, User user) {
        CourseMemberSummary summary = new CourseMemberSummary();
        summary.setId(member.getId());
        summary.setCourseId(member.getCourseId());
        summary.setUserId(member.getUserId());
        summary.setUsername(user.getUsername());
        summary.setRealName(user.getRealName());
        summary.setMemberRole(member.getMemberRole());
        summary.setStatus(member.getStatus());
        summary.setJoinedAt(member.getJoinedAt());
        return summary;
    }

    private boolean userHasRole(Long userId, String roleCode) {
        for (Role role : roleService.listByUserId(userId)) {
            if (roleCode.equals(role.getCode())) {
                return true;
            }
        }
        return false;
    }

    private void ensureLogin(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(401, "请先登录");
        }
    }

    private boolean isAdmin(UserPrincipal principal) {
        return hasRole(principal, "ADMIN");
    }

    private boolean hasRole(UserPrincipal principal, String role) {
        return principal != null && principal.getRoles().contains(role);
    }

    private void recordDenied(UserPrincipal principal, String ip, String operation, Long targetId, String detail) {
        operationLogService.record(principal.getId(), principal.getUsername(), operation,
                "COURSE", targetId, ip, "DENIED", detail);
    }
}
