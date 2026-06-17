package com.coursework.system.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.assignment.service.AssignmentService;
import com.coursework.system.common.exception.BusinessException;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.course.dto.CourseSummary;
import com.coursework.system.course.entity.CourseApplication;
import com.coursework.system.course.service.CourseApplicationService;
import com.coursework.system.course.service.CourseService;
import com.coursework.system.dashboard.dto.DashboardData;
import com.coursework.system.dashboard.service.DashboardService;
import com.coursework.system.duplicate.entity.DuplicateRecord;
import com.coursework.system.duplicate.service.DuplicateRecordService;
import com.coursework.system.log.service.OperationLogService;
import com.coursework.system.notification.service.NotificationService;
import com.coursework.system.submission.entity.Grade;
import com.coursework.system.submission.entity.Submission;
import com.coursework.system.submission.service.GradeService;
import com.coursework.system.submission.service.SubmissionService;
import com.coursework.system.user.dto.UserSummary;
import com.coursework.system.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final CourseService courseService;
    private final CourseApplicationService courseApplicationService;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final GradeService gradeService;
    private final DuplicateRecordService duplicateRecordService;
    private final NotificationService notificationService;
    private final OperationLogService operationLogService;
    private final UserService userService;

    public DashboardServiceImpl(CourseService courseService,
                                CourseApplicationService courseApplicationService,
                                AssignmentService assignmentService,
                                SubmissionService submissionService,
                                GradeService gradeService,
                                DuplicateRecordService duplicateRecordService,
                                NotificationService notificationService,
                                OperationLogService operationLogService,
                                UserService userService) {
        this.courseService = courseService;
        this.courseApplicationService = courseApplicationService;
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
        this.gradeService = gradeService;
        this.duplicateRecordService = duplicateRecordService;
        this.notificationService = notificationService;
        this.operationLogService = operationLogService;
        this.userService = userService;
    }

    @Override
    public DashboardData getDashboard(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(401, "请先登录");
        }
        DashboardData data = new DashboardData();
        data.setGeneratedAt(LocalDateTime.now());
        data.setPrimaryRole(primaryRole(principal));
        data.setUnreadNotificationCount(notificationService.unreadCount(principal.getId()));
        data.setRecentNotifications(limit(notificationService.listForUser(principal.getId()), 6));

        if (hasRole(principal, "ADMIN")) {
            fillAdmin(data);
            return data;
        }

        List<CourseSummary> courses = courseService.listMyCourses(principal);
        data.setRecentCourses(limit(courses, 6));
        data.setMyCourseCount((long) courses.size());
        List<Long> courseIds = courses.stream().map(CourseSummary::getId).collect(Collectors.toList());
        List<Assignment> assignments = listActiveAssignments(courseIds);
        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).collect(Collectors.toList());
        data.setAssignmentCount((long) assignments.size());

        if (hasRole(principal, "STUDENT")) {
            fillStudent(data, assignments, principal);
        } else {
            fillReviewer(data, assignmentIds);
        }
        return data;
    }

    private void fillAdmin(DashboardData data) {
        List<UserSummary> users = userService.listSummaries();
        data.setUserCount((long) users.size());
        data.setTeacherCount(countUsersByRole(users, "TEACHER"));
        data.setStudentCount(countUsersByRole(users, "STUDENT"));
        data.setAssistantCount(countUsersByRole(users, "ASSISTANT"));
        data.setPendingCourseApplications(courseApplicationService.count(new QueryWrapper<CourseApplication>().eq("status", "PENDING")));
        data.setApprovedCourseApplications(courseApplicationService.count(new QueryWrapper<CourseApplication>().eq("status", "APPROVED")));
        data.setRecentApplications(limit(courseApplicationService.listPending(), 6));
        data.setRecentCourses(limit(courseService.listAllCourses(), 6));
        data.setRecentLogs(operationLogService.latest(8));
    }

    private void fillReviewer(DashboardData data, List<Long> assignmentIds) {
        if (assignmentIds.isEmpty()) {
            data.setPendingGradingCount(0L);
            data.setGradedCount(0L);
            data.setDuplicateCount(0L);
            return;
        }
        List<Submission> submissions = submissionService.list(new QueryWrapper<Submission>().in("assignment_id", assignmentIds));
        List<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());
        data.setPendingGradingCount(countGrades(submissionIds, "PENDING"));
        data.setGradedCount(countGrades(submissionIds, "GRADED"));
        data.setDuplicateCount(duplicateRecordService.count(new QueryWrapper<DuplicateRecord>().in("assignment_id", assignmentIds)));
    }

    private void fillStudent(DashboardData data, List<Assignment> assignments, UserPrincipal principal) {
        long submitted = 0;
        long graded = 0;
        long pending = 0;
        for (Assignment assignment : assignments) {
            Submission submission = submissionService.getOne(new QueryWrapper<Submission>()
                    .eq("assignment_id", assignment.getId())
                    .eq("student_id", principal.getId())
                    .orderByDesc("submit_time")
                    .last("LIMIT 1"), false);
            if (submission == null) {
                pending++;
                continue;
            }
            submitted++;
            if (submission.getFinalScore() != null || "GRADED".equals(submission.getStatus()) || "AUTO_GRADED".equals(submission.getStatus())) {
                graded++;
            }
        }
        data.setPendingAssignmentCount(pending);
        data.setSubmittedAssignmentCount(submitted);
        data.setGradedCount(graded);
    }

    private List<Assignment> listActiveAssignments(List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return Collections.emptyList();
        }
        return assignmentService.list(new QueryWrapper<Assignment>()
                .in("course_id", courseIds)
                .ne("status", "DELETED")
                .orderByAsc("deadline"));
    }

    private Long countGrades(List<Long> submissionIds, String status) {
        if (submissionIds.isEmpty()) {
            return 0L;
        }
        return gradeService.count(new QueryWrapper<Grade>()
                .in("submission_id", submissionIds)
                .eq("grade_status", status));
    }

    private Long countUsersByRole(List<UserSummary> users, String role) {
        return users.stream()
                .filter(user -> user.getRoles() != null && user.getRoles().contains(role))
                .count();
    }

    private String primaryRole(UserPrincipal principal) {
        if (hasRole(principal, "ADMIN")) return "ADMIN";
        if (hasRole(principal, "TEACHER")) return "TEACHER";
        if (hasRole(principal, "ASSISTANT")) return "ASSISTANT";
        return "STUDENT";
    }

    private boolean hasRole(UserPrincipal principal, String role) {
        return principal.getRoles() != null && principal.getRoles().contains(role);
    }

    private <T> List<T> limit(List<T> input, int size) {
        if (input == null || input.isEmpty()) {
            return new ArrayList<T>();
        }
        return input.size() <= size ? input : new ArrayList<T>(input.subList(0, size));
    }
}
