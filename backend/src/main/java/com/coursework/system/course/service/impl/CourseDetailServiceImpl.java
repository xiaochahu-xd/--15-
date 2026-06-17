package com.coursework.system.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.assignment.service.AssignmentService;
import com.coursework.system.common.exception.BusinessException;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.course.dto.CourseDetailSummary;
import com.coursework.system.course.dto.CourseSummary;
import com.coursework.system.course.entity.CourseMember;
import com.coursework.system.course.service.CourseDetailService;
import com.coursework.system.course.service.CourseMemberService;
import com.coursework.system.course.service.CourseService;
import com.coursework.system.duplicate.entity.DuplicateRecord;
import com.coursework.system.duplicate.service.DuplicateRecordService;
import com.coursework.system.statistics.dto.CourseStatistics;
import com.coursework.system.statistics.service.StatisticsService;
import com.coursework.system.submission.entity.Grade;
import com.coursework.system.submission.entity.Submission;
import com.coursework.system.submission.service.GradeService;
import com.coursework.system.submission.service.SubmissionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseDetailServiceImpl implements CourseDetailService {
    private final CourseService courseService;
    private final CourseMemberService courseMemberService;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final GradeService gradeService;
    private final DuplicateRecordService duplicateRecordService;
    private final StatisticsService statisticsService;

    public CourseDetailServiceImpl(CourseService courseService,
                                   CourseMemberService courseMemberService,
                                   AssignmentService assignmentService,
                                   SubmissionService submissionService,
                                   GradeService gradeService,
                                   DuplicateRecordService duplicateRecordService,
                                   StatisticsService statisticsService) {
        this.courseService = courseService;
        this.courseMemberService = courseMemberService;
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
        this.gradeService = gradeService;
        this.duplicateRecordService = duplicateRecordService;
        this.statisticsService = statisticsService;
    }

    @Override
    public CourseDetailSummary getDetail(Long courseId, UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!courseService.canViewCourse(courseId, principal)) {
            throw new BusinessException(403, "无权查看该课程");
        }

        CourseSummary course = findCourseSummary(courseId, principal);
        List<Assignment> assignments = assignmentService.list(new QueryWrapper<Assignment>()
                .eq("course_id", courseId)
                .ne("status", "DELETED"));
        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).collect(Collectors.toList());
        List<Submission> submissions = assignmentIds.isEmpty()
                ? Collections.<Submission>emptyList()
                : submissionService.list(new QueryWrapper<Submission>().in("assignment_id", assignmentIds));
        List<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());

        CourseDetailSummary detail = new CourseDetailSummary();
        detail.setCourse(course);
        detail.setMyRole(course == null ? null : course.getMemberRole());
        detail.setMemberCount(courseMemberService.count(new QueryWrapper<CourseMember>()
                .eq("course_id", courseId)
                .eq("status", 1)));
        detail.setAssignmentCount((long) assignments.size());
        detail.setSubmissionCount((long) submissions.size());
        detail.setPendingSubmissionCount(calculatePendingSubmissionCount(courseId, principal, assignments));
        detail.setPendingGradingCount(countGrades(submissionIds, "PENDING"));
        detail.setDuplicateCount(assignmentIds.isEmpty() ? 0L : duplicateRecordService.count(new QueryWrapper<DuplicateRecord>().in("assignment_id", assignmentIds)));
        if (canViewWholeCourseStatistics(principal)) {
            CourseStatistics statistics = statisticsService.getCourseStatistics(courseId, principal);
            detail.setAverageScore(statistics.getAverageScore() == null ? BigDecimal.ZERO : statistics.getAverageScore());
            detail.setSubmissionRate(statistics.getSubmissionRate() == null ? BigDecimal.ZERO : statistics.getSubmissionRate());
        } else {
            detail.setAverageScore(BigDecimal.ZERO);
            detail.setSubmissionRate(BigDecimal.ZERO);
        }
        return detail;
    }

    private CourseSummary findCourseSummary(Long courseId, UserPrincipal principal) {
        for (CourseSummary summary : courseService.listMyCourses(principal)) {
            if (summary.getId().equals(courseId)) {
                return summary;
            }
        }
        throw new BusinessException(404, "课程不存在");
    }

    private Long calculatePendingSubmissionCount(Long courseId, UserPrincipal principal, List<Assignment> assignments) {
        if (principal.getRoles() != null && principal.getRoles().contains("STUDENT")) {
            long pending = 0;
            for (Assignment assignment : assignments) {
                long count = submissionService.count(new QueryWrapper<Submission>()
                        .eq("assignment_id", assignment.getId())
                        .eq("student_id", principal.getId()));
                if (count == 0) {
                    pending++;
                }
            }
            return pending;
        }
        long studentCount = courseMemberService.count(new QueryWrapper<CourseMember>()
                .eq("course_id", courseId)
                .eq("member_role", "STUDENT")
                .eq("status", 1));
        long expected = studentCount * assignments.size();
        return Math.max(expected - totalSubmissions(assignments), 0);
    }

    private long totalSubmissions(List<Assignment> assignments) {
        if (assignments.isEmpty()) {
            return 0;
        }
        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).collect(Collectors.toList());
        return submissionService.count(new QueryWrapper<Submission>().in("assignment_id", assignmentIds));
    }

    private Long countGrades(List<Long> submissionIds, String status) {
        if (submissionIds.isEmpty()) {
            return 0L;
        }
        return gradeService.count(new QueryWrapper<Grade>()
                .in("submission_id", submissionIds)
                .eq("grade_status", status));
    }

    private boolean canViewWholeCourseStatistics(UserPrincipal principal) {
        return principal.getRoles() != null
                && (principal.getRoles().contains("ADMIN")
                || principal.getRoles().contains("TEACHER")
                || principal.getRoles().contains("ASSISTANT"));
    }
}
