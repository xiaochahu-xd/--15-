package com.coursework.system.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursework.system.common.exception.BusinessException;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.course.dto.CourseApplicationCreateRequest;
import com.coursework.system.course.dto.CourseApplicationRejectRequest;
import com.coursework.system.course.dto.CourseApplicationSummary;
import com.coursework.system.course.entity.Course;
import com.coursework.system.course.entity.CourseApplication;
import com.coursework.system.course.entity.CourseMember;
import com.coursework.system.course.mapper.CourseApplicationMapper;
import com.coursework.system.course.service.CourseApplicationService;
import com.coursework.system.course.service.CourseMemberService;
import com.coursework.system.course.service.CourseService;
import com.coursework.system.event.CourseApplicationCreatedEvent;
import com.coursework.system.event.CourseApprovedEvent;
import com.coursework.system.event.EventManager;
import com.coursework.system.log.service.OperationLogService;
import com.coursework.system.user.entity.User;
import com.coursework.system.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseApplicationServiceImpl extends ServiceImpl<CourseApplicationMapper, CourseApplication>
        implements CourseApplicationService {
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final CourseService courseService;
    private final CourseMemberService courseMemberService;
    private final UserService userService;
    private final OperationLogService operationLogService;
    private final EventManager eventManager;

    public CourseApplicationServiceImpl(CourseService courseService,
                                        CourseMemberService courseMemberService,
                                        UserService userService,
                                        OperationLogService operationLogService,
                                        EventManager eventManager) {
        this.courseService = courseService;
        this.courseMemberService = courseMemberService;
        this.userService = userService;
        this.operationLogService = operationLogService;
        this.eventManager = eventManager;
    }

    @Override
    @Transactional
    public CourseApplicationSummary submitApplication(CourseApplicationCreateRequest request,
                                                      UserPrincipal principal,
                                                      String ip) {
        ensureRole(principal, "TEACHER", "只有教师可以申请创建课程");
        String courseCode = normalizeCode(request.getCourseCode());
        if (courseService.getByCourseCode(courseCode) != null) {
            throw new BusinessException(400, "课程代码已存在");
        }
        long pendingCount = count(new QueryWrapper<CourseApplication>()
                .eq("course_code", courseCode)
                .eq("status", STATUS_PENDING));
        if (pendingCount > 0) {
            throw new BusinessException(400, "该课程代码已有待审批申请");
        }

        CourseApplication application = new CourseApplication();
        application.setTeacherId(principal.getId());
        application.setCourseName(request.getCourseName().trim());
        application.setCourseCode(courseCode);
        application.setDescription(request.getDescription());
        application.setStatus(STATUS_PENDING);
        application.setCreatedAt(LocalDateTime.now());
        save(application);
        eventManager.publish(new CourseApplicationCreatedEvent(application.getId(), principal.getId(),
                principal.getUsername(), application.getCourseName(), application.getCourseCode(), ip));
        operationLogService.record(principal.getId(), principal.getUsername(), "COURSE_APPLICATION_SUBMIT",
                "COURSE_APPLICATION", application.getId(), ip, "SUCCESS", "提交课程申请：" + courseCode);
        return toSummary(application);
    }

    @Override
    public List<CourseApplicationSummary> listPending() {
        return baseMapper.selectPendingSummaries();
    }

    @Override
    @Transactional
    public CourseApplicationSummary approve(Long applicationId, UserPrincipal principal, String ip) {
        ensureRole(principal, "ADMIN", "只有管理员可以审批课程申请");
        CourseApplication application = getById(applicationId);
        ensurePending(application);
        if (courseService.getByCourseCode(application.getCourseCode()) != null) {
            throw new BusinessException(400, "课程代码已被正式课程占用");
        }

        Course course = new Course();
        course.setCourseName(application.getCourseName());
        course.setCourseCode(application.getCourseCode());
        course.setDescription(application.getDescription());
        course.setOwnerId(application.getTeacherId());
        course.setStatus("ACTIVE");
        course.setCreatedAt(LocalDateTime.now());
        courseService.save(course);

        CourseMember ownerMember = new CourseMember();
        ownerMember.setCourseId(course.getId());
        ownerMember.setUserId(application.getTeacherId());
        ownerMember.setMemberRole("OWNER");
        ownerMember.setJoinedAt(LocalDateTime.now());
        ownerMember.setStatus(1);
        courseMemberService.save(ownerMember);

        application.setStatus(STATUS_APPROVED);
        application.setReviewedBy(principal.getId());
        application.setReviewedAt(LocalDateTime.now());
        updateById(application);

        eventManager.publish(new CourseApprovedEvent(application.getId(), application.getTeacherId(),
                application.getCourseName(), application.getCourseCode(), true, null,
                principal.getId(), principal.getUsername(), ip));
        operationLogService.record(principal.getId(), principal.getUsername(), "COURSE_APPLICATION_APPROVE",
                "COURSE_APPLICATION", application.getId(), ip, "SUCCESS", "审批通过：" + application.getCourseCode());
        return toSummary(application);
    }

    @Override
    @Transactional
    public CourseApplicationSummary reject(Long applicationId,
                                           CourseApplicationRejectRequest request,
                                           UserPrincipal principal,
                                           String ip) {
        ensureRole(principal, "ADMIN", "只有管理员可以审批课程申请");
        CourseApplication application = getById(applicationId);
        ensurePending(application);
        application.setStatus(STATUS_REJECTED);
        application.setRejectReason(request.getRejectReason().trim());
        application.setReviewedBy(principal.getId());
        application.setReviewedAt(LocalDateTime.now());
        updateById(application);

        eventManager.publish(new CourseApprovedEvent(application.getId(), application.getTeacherId(),
                application.getCourseName(), application.getCourseCode(), false, application.getRejectReason(),
                principal.getId(), principal.getUsername(), ip));
        operationLogService.record(principal.getId(), principal.getUsername(), "COURSE_APPLICATION_REJECT",
                "COURSE_APPLICATION", application.getId(), ip, "SUCCESS", "驳回：" + application.getCourseCode());
        return toSummary(application);
    }

    private void ensurePending(CourseApplication application) {
        if (application == null) {
            throw new BusinessException(404, "课程申请不存在");
        }
        if (!STATUS_PENDING.equals(application.getStatus())) {
            throw new BusinessException(400, "该申请已处理，不能重复审批");
        }
    }

    private void ensureRole(UserPrincipal principal, String role, String message) {
        if (principal == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!principal.getRoles().contains(role)) {
            throw new BusinessException(403, message);
        }
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    private CourseApplicationSummary toSummary(CourseApplication application) {
        CourseApplicationSummary summary = new CourseApplicationSummary();
        summary.setId(application.getId());
        summary.setTeacherId(application.getTeacherId());
        User teacher = userService.getById(application.getTeacherId());
        summary.setTeacherName(teacher == null ? "" : teacher.getRealName());
        summary.setCourseName(application.getCourseName());
        summary.setCourseCode(application.getCourseCode());
        summary.setDescription(application.getDescription());
        summary.setStatus(application.getStatus());
        summary.setRejectReason(application.getRejectReason());
        summary.setReviewedBy(application.getReviewedBy());
        if (application.getReviewedBy() != null) {
            User reviewer = userService.getById(application.getReviewedBy());
            summary.setReviewedByName(reviewer == null ? "" : reviewer.getRealName());
        }
        summary.setReviewedAt(application.getReviewedAt());
        summary.setCreatedAt(application.getCreatedAt());
        return summary;
    }
}
