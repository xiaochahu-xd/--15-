package com.coursework.system.assignment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursework.system.assignment.dto.AssignmentCreateRequest;
import com.coursework.system.assignment.dto.AssignmentSummary;
import com.coursework.system.assignment.dto.AssignmentUpdateRequest;
import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.assignment.mapper.AssignmentMapper;
import com.coursework.system.assignment.service.AssignmentService;
import com.coursework.system.assignment.service.GradingRuleService;
import com.coursework.system.common.exception.BusinessException;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.course.entity.Course;
import com.coursework.system.course.entity.CourseMember;
import com.coursework.system.course.service.CourseMemberService;
import com.coursework.system.course.service.CourseService;
import com.coursework.system.event.AssignmentPublishedEvent;
import com.coursework.system.event.EventManager;
import com.coursework.system.log.service.OperationLogService;
import com.coursework.system.user.entity.User;
import com.coursework.system.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

@Service
public class AssignmentServiceImpl extends ServiceImpl<AssignmentMapper, Assignment> implements AssignmentService {
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_DELETED = "DELETED";
    private static final Set<String> SUPPORTED_TYPES =
            new HashSet<String>(Arrays.asList("TEXT", "FILE", "SINGLE_CHOICE", "TRUE_FALSE"));

    private final CourseService courseService;
    private final CourseMemberService courseMemberService;
    private final UserService userService;
    private final GradingRuleService gradingRuleService;
    private final EventManager eventManager;
    private final OperationLogService operationLogService;

    public AssignmentServiceImpl(CourseService courseService,
                                 CourseMemberService courseMemberService,
                                 UserService userService,
                                 GradingRuleService gradingRuleService,
                                 EventManager eventManager,
                                 OperationLogService operationLogService) {
        this.courseService = courseService;
        this.courseMemberService = courseMemberService;
        this.userService = userService;
        this.gradingRuleService = gradingRuleService;
        this.eventManager = eventManager;
        this.operationLogService = operationLogService;
    }

    @Override
    @Transactional
    public AssignmentSummary createAssignment(Long courseId, AssignmentCreateRequest request, UserPrincipal principal, String ip) {
        ensureLogin(principal);
        if (!canEditCourseAssignments(courseId, principal)) {
            recordDenied(principal, ip, "ASSIGNMENT_CREATE", courseId, "无权发布作业");
            throw new BusinessException(403, "只有课程负责人或课程教师可以发布作业");
        }
        validateAssignmentRequest(request);
        Assignment assignment = new Assignment();
        assignment.setCourseId(courseId);
        fillAssignment(assignment, request);
        assignment.setStatus(STATUS_PUBLISHED);
        assignment.setCreatedBy(principal.getId());
        assignment.setCreatedAt(LocalDateTime.now());
        save(assignment);
        gradingRuleService.createDefaultRules(assignment.getId(), assignment.getAssignmentType(), assignment.getAllowLate() != null && assignment.getAllowLate() == 1);
        eventManager.publishAssignmentPublished(new AssignmentPublishedEvent(courseId, assignment.getId(),
                assignment.getTitle(), principal.getId(), principal.getUsername(), ip));
        operationLogService.record(principal.getId(), principal.getUsername(), "ASSIGNMENT_CREATE",
                "ASSIGNMENT", assignment.getId(), ip, "SUCCESS", "发布作业：" + assignment.getTitle());
        return toSummary(assignment);
    }

    @Override
    public List<AssignmentSummary> listByCourse(Long courseId, UserPrincipal principal) {
        ensureLogin(principal);
        if (!courseService.canViewCourse(courseId, principal)) {
            throw new BusinessException(403, "无权查看该课程作业");
        }
        List<Assignment> assignments = list(new QueryWrapper<Assignment>()
                .eq("course_id", courseId)
                .ne("status", STATUS_DELETED)
                .orderByDesc("created_at"));
        List<AssignmentSummary> summaries = new ArrayList<AssignmentSummary>();
        for (Assignment assignment : assignments) {
            summaries.add(toSummary(assignment));
        }
        return summaries;
    }

    @Override
    public AssignmentSummary getDetail(Long assignmentId, UserPrincipal principal) {
        ensureLogin(principal);
        Assignment assignment = getActiveAssignment(assignmentId);
        if (!canViewAssignment(assignment, principal)) {
            throw new BusinessException(403, "无权查看该作业");
        }
        return toSummary(assignment);
    }

    @Override
    @Transactional
    public AssignmentSummary updateAssignment(Long assignmentId, AssignmentUpdateRequest request, UserPrincipal principal, String ip) {
        ensureLogin(principal);
        Assignment assignment = getActiveAssignment(assignmentId);
        if (!canEditAssignment(assignment, principal)) {
            recordDenied(principal, ip, "ASSIGNMENT_UPDATE", assignmentId, "无权修改作业");
            throw new BusinessException(403, "只有课程负责人或课程教师可以修改作业");
        }
        validateAssignmentRequest(request);
        fillAssignment(assignment, request);
        updateById(assignment);
        gradingRuleService.createDefaultRules(assignment.getId(), assignment.getAssignmentType(), assignment.getAllowLate() != null && assignment.getAllowLate() == 1);
        operationLogService.record(principal.getId(), principal.getUsername(), "ASSIGNMENT_UPDATE",
                "ASSIGNMENT", assignment.getId(), ip, "SUCCESS", "修改作业：" + assignment.getTitle());
        return toSummary(assignment);
    }

    @Override
    @Transactional
    public void deleteAssignment(Long assignmentId, UserPrincipal principal, String ip) {
        ensureLogin(principal);
        Assignment assignment = getActiveAssignment(assignmentId);
        if (!canEditAssignment(assignment, principal)) {
            recordDenied(principal, ip, "ASSIGNMENT_DELETE", assignmentId, "无权删除作业");
            throw new BusinessException(403, "只有课程负责人或课程教师可以删除作业");
        }
        assignment.setStatus(STATUS_DELETED);
        updateById(assignment);
        operationLogService.record(principal.getId(), principal.getUsername(), "ASSIGNMENT_DELETE",
                "ASSIGNMENT", assignment.getId(), ip, "SUCCESS", "删除作业：" + assignment.getTitle());
    }

    @Override
    public Assignment getActiveAssignment(Long assignmentId) {
        Assignment assignment = getById(assignmentId);
        if (assignment == null || STATUS_DELETED.equals(assignment.getStatus())) {
            throw new BusinessException(404, "作业不存在");
        }
        return assignment;
    }

    @Override
    public boolean canEditAssignment(Assignment assignment, UserPrincipal principal) {
        return assignment != null && canEditCourseAssignments(assignment.getCourseId(), principal);
    }

    @Override
    public boolean canViewAssignment(Assignment assignment, UserPrincipal principal) {
        return assignment != null && courseService.canViewCourse(assignment.getCourseId(), principal);
    }

    private boolean canEditCourseAssignments(Long courseId, UserPrincipal principal) {
        if (principal == null) {
            return false;
        }
        if (principal.getRoles().contains("ADMIN")) {
            return true;
        }
        Course course = courseService.getById(courseId);
        if (course == null || !"ACTIVE".equals(course.getStatus())) {
            return false;
        }
        if (course.getOwnerId().equals(principal.getId()) && principal.getRoles().contains("TEACHER")) {
            return true;
        }
        CourseMember member = courseMemberService.getByCourseIdAndUserId(courseId, principal.getId());
        return member != null
                && member.getStatus() != null
                && member.getStatus() == 1
                && "TEACHER".equals(member.getMemberRole())
                && principal.getRoles().contains("TEACHER");
    }

    private void fillAssignment(Assignment assignment, AssignmentCreateRequest request) {
        assignment.setTitle(request.getTitle().trim());
        assignment.setDescription(request.getDescription());
        assignment.setAssignmentType(normalizeType(request.getAssignmentType()));
        assignment.setDeadline(request.getDeadline());
        assignment.setTotalScore(request.getTotalScore());
        assignment.setAllowLate(Boolean.TRUE.equals(request.getAllowLate()) ? 1 : 0);
    }

    private void validateAssignmentRequest(AssignmentCreateRequest request) {
        String assignmentType = normalizeType(request.getAssignmentType());
        if (!SUPPORTED_TYPES.contains(assignmentType)) {
            throw new BusinessException(400, "作业类型只能是 TEXT、FILE、SINGLE_CHOICE 或 TRUE_FALSE，多选题暂未实现");
        }
        if (request.getDeadline() == null || !request.getDeadline().isAfter(LocalDateTime.now())) {
            throw new BusinessException(400, "作业截止时间必须晚于当前时间");
        }
        if (request.getTotalScore() == null || request.getTotalScore().signum() <= 0) {
            throw new BusinessException(400, "作业满分必须大于0");
        }
    }

    private AssignmentSummary toSummary(Assignment assignment) {
        AssignmentSummary summary = new AssignmentSummary();
        summary.setId(assignment.getId());
        summary.setCourseId(assignment.getCourseId());
        Course course = courseService.getById(assignment.getCourseId());
        summary.setCourseName(course == null ? "" : course.getCourseName());
        summary.setTitle(assignment.getTitle());
        summary.setDescription(assignment.getDescription());
        summary.setAssignmentType(assignment.getAssignmentType());
        summary.setDeadline(assignment.getDeadline());
        summary.setTotalScore(assignment.getTotalScore());
        summary.setAllowLate(assignment.getAllowLate() != null && assignment.getAllowLate() == 1);
        summary.setStatus(assignment.getStatus());
        summary.setCreatedBy(assignment.getCreatedBy());
        User creator = userService.getById(assignment.getCreatedBy());
        summary.setCreatedByName(creator == null ? "" : creator.getRealName());
        summary.setCreatedAt(assignment.getCreatedAt());
        return summary;
    }

    private String normalizeType(String type) {
        return type == null ? "" : type.trim().toUpperCase();
    }

    private void ensureLogin(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(401, "请先登录");
        }
    }

    private void recordDenied(UserPrincipal principal, String ip, String operation, Long targetId, String detail) {
        operationLogService.record(principal.getId(), principal.getUsername(), operation,
                "ASSIGNMENT", targetId, ip, "DENIED", detail);
    }
}
