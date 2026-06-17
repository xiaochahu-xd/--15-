package com.coursework.system.grading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.assignment.service.AssignmentService;
import com.coursework.system.common.exception.BusinessException;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.course.entity.Course;
import com.coursework.system.course.entity.CourseMember;
import com.coursework.system.course.service.CourseMemberService;
import com.coursework.system.course.service.CourseService;
import com.coursework.system.duplicate.service.DuplicateRecordService;
import com.coursework.system.event.AssignmentReturnedEvent;
import com.coursework.system.event.EventManager;
import com.coursework.system.event.GradeCompletedEvent;
import com.coursework.system.grading.dto.GradeRequest;
import com.coursework.system.grading.dto.GradingDetail;
import com.coursework.system.grading.dto.GradingItem;
import com.coursework.system.grading.dto.GradingProgress;
import com.coursework.system.grading.dto.ReturnSubmissionRequest;
import com.coursework.system.grading.service.GradingService;
import com.coursework.system.submission.dto.FileRecordSummary;
import com.coursework.system.submission.entity.FileRecord;
import com.coursework.system.submission.entity.Grade;
import com.coursework.system.submission.entity.Submission;
import com.coursework.system.submission.mapper.SubmissionMapper;
import com.coursework.system.submission.service.FileRecordService;
import com.coursework.system.submission.service.GradeService;
import com.coursework.system.user.entity.User;
import com.coursework.system.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GradingServiceImpl implements GradingService {
    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final CourseMemberService courseMemberService;
    private final UserService userService;
    private final SubmissionMapper submissionMapper;
    private final FileRecordService fileRecordService;
    private final GradeService gradeService;
    private final DuplicateRecordService duplicateRecordService;
    private final EventManager eventManager;

    public GradingServiceImpl(AssignmentService assignmentService,
                              CourseService courseService,
                              CourseMemberService courseMemberService,
                              UserService userService,
                              SubmissionMapper submissionMapper,
                              FileRecordService fileRecordService,
                              GradeService gradeService,
                              DuplicateRecordService duplicateRecordService,
                              EventManager eventManager) {
        this.assignmentService = assignmentService;
        this.courseService = courseService;
        this.courseMemberService = courseMemberService;
        this.userService = userService;
        this.submissionMapper = submissionMapper;
        this.fileRecordService = fileRecordService;
        this.gradeService = gradeService;
        this.duplicateRecordService = duplicateRecordService;
        this.eventManager = eventManager;
    }

    @Override
    public List<GradingItem> listGradingList(Long assignmentId, UserPrincipal principal) {
        ensureLogin(principal);
        Assignment assignment = assignmentService.getActiveAssignment(assignmentId);
        if (!canReview(assignment, principal)) {
            throw new BusinessException(403, "无权查看该作业批改列表");
        }
        List<Submission> submissions = submissionMapper.selectList(new QueryWrapper<Submission>()
                .eq("assignment_id", assignmentId)
                .orderByDesc("submit_time"));
        List<GradingItem> items = new ArrayList<GradingItem>();
        for (Submission submission : submissions) {
            items.add(toItem(submission, assignment));
        }
        return items;
    }

    @Override
    public GradingDetail getSubmissionDetail(Long submissionId, UserPrincipal principal) {
        ensureLogin(principal);
        Submission submission = requireSubmission(submissionId);
        Assignment assignment = assignmentService.getActiveAssignment(submission.getAssignmentId());
        if (!canReview(assignment, principal)) {
            throw new BusinessException(403, "无权查看该提交详情");
        }
        return toDetail(submission, assignment);
    }

    @Override
    @Transactional
    public GradingDetail saveGrade(Long submissionId, GradeRequest request, UserPrincipal principal, String ip) {
        ensureLogin(principal);
        Submission submission = requireSubmission(submissionId);
        Assignment assignment = assignmentService.getActiveAssignment(submission.getAssignmentId());
        if (!canGrade(assignment, principal)) {
            throw new BusinessException(403, "无权批改该提交");
        }
        if (assignment.getTotalScore() != null && request.getScore().compareTo(assignment.getTotalScore()) > 0) {
            throw new BusinessException(400, "分数不能超过作业满分");
        }

        Grade grade = gradeService.getBySubmissionId(submissionId);
        if (grade == null) {
            grade = new Grade();
            grade.setSubmissionId(submissionId);
        }
        grade.setGraderId(principal.getId());
        grade.setScore(request.getScore());
        grade.setComment(trimToNull(request.getComment()));
        grade.setGradedAt(LocalDateTime.now());
        grade.setGradeStatus("GRADED");
        gradeService.saveOrUpdate(grade);

        submission.setFinalScore(request.getScore());
        submission.setStatus("GRADED");
        submissionMapper.updateById(submission);

        eventManager.publishGradeCompleted(new GradeCompletedEvent(submission.getId(), assignment.getId(),
                assignment.getTitle(), submission.getStudentId(), principal.getId(), principal.getUsername(),
                request.getScore(), ip));
        return toDetail(submission, assignment);
    }

    @Override
    @Transactional
    public GradingDetail returnSubmission(Long submissionId, ReturnSubmissionRequest request,
                                          UserPrincipal principal, String ip) {
        ensureLogin(principal);
        Submission submission = requireSubmission(submissionId);
        Assignment assignment = assignmentService.getActiveAssignment(submission.getAssignmentId());
        if (!canGrade(assignment, principal)) {
            throw new BusinessException(403, "无权退回该提交");
        }
        String reason = trimToNull(request == null ? null : request.getReason());

        Grade grade = gradeService.getBySubmissionId(submissionId);
        if (grade == null) {
            grade = new Grade();
            grade.setSubmissionId(submissionId);
        }
        grade.setGraderId(principal.getId());
        grade.setComment(reason);
        grade.setGradedAt(LocalDateTime.now());
        grade.setGradeStatus("RETURNED");
        gradeService.saveOrUpdate(grade);

        submission.setFinalScore(null);
        submission.setStatus("RETURNED");
        submissionMapper.updateById(submission);

        eventManager.publishAssignmentReturned(new AssignmentReturnedEvent(submission.getId(), assignment.getId(),
                assignment.getTitle(), submission.getStudentId(), principal.getId(), principal.getUsername(),
                reason, ip));
        return toDetail(submission, assignment);
    }

    @Override
    public GradingDetail getGrade(Long submissionId, UserPrincipal principal) {
        ensureLogin(principal);
        Submission submission = requireSubmission(submissionId);
        Assignment assignment = assignmentService.getActiveAssignment(submission.getAssignmentId());
        if (principal.getRoles().contains("STUDENT")) {
            if (!submission.getStudentId().equals(principal.getId())) {
                throw new BusinessException(403, "学生只能查看自己的成绩和评语");
            }
        } else if (!canReview(assignment, principal)) {
            throw new BusinessException(403, "无权查看该成绩记录");
        }
        return toDetail(submission, assignment);
    }

    @Override
    public GradingProgress getCourseProgress(Long courseId, UserPrincipal principal) {
        ensureLogin(principal);
        Course course = courseService.getById(courseId);
        if (course == null || !"ACTIVE".equals(course.getStatus())) {
            throw new BusinessException(404, "课程不存在");
        }
        if (!canViewCourse(course, principal)) {
            throw new BusinessException(403, "无权查看该课程批改进度");
        }

        List<Assignment> assignments = assignmentService.list(new QueryWrapper<Assignment>()
                .eq("course_id", courseId)
                .ne("status", "DELETED"));
        List<Long> assignmentIds = new ArrayList<Long>();
        for (Assignment assignment : assignments) {
            assignmentIds.add(assignment.getId());
        }

        List<Submission> submissions = assignmentIds.isEmpty()
                ? new ArrayList<Submission>()
                : submissionMapper.selectList(new QueryWrapper<Submission>().in("assignment_id", assignmentIds));
        List<Long> submissionIds = new ArrayList<Long>();
        for (Submission submission : submissions) {
            submissionIds.add(submission.getId());
        }

        Map<Long, Grade> gradesBySubmissionId = loadGrades(submissionIds);
        int gradedCount = 0;
        int returnedCount = 0;
        int autoGradedCount = 0;
        int pendingCount = 0;
        for (Submission submission : submissions) {
            Grade grade = gradesBySubmissionId.get(submission.getId());
            String status = grade == null ? "PENDING" : grade.getGradeStatus();
            if ("GRADED".equals(status)) {
                gradedCount++;
            } else if ("RETURNED".equals(status)) {
                returnedCount++;
            } else if ("AUTO_GRADED".equals(status)) {
                autoGradedCount++;
            } else {
                pendingCount++;
            }
        }

        GradingProgress progress = new GradingProgress();
        progress.setCourseId(course.getId());
        progress.setCourseName(course.getCourseName());
        progress.setAssignmentCount(assignments.size());
        progress.setSubmissionCount(submissions.size());
        progress.setPendingCount(pendingCount);
        progress.setAutoGradedCount(autoGradedCount);
        progress.setGradedCount(gradedCount);
        progress.setReturnedCount(returnedCount);
        progress.setGradingRate(calculateRate(gradedCount, submissions.size()));
        return progress;
    }

    private Submission requireSubmission(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(404, "提交记录不存在");
        }
        return submission;
    }

    private boolean canGrade(Assignment assignment, UserPrincipal principal) {
        if (principal == null || assignment == null) {
            return false;
        }
        boolean hasGradingRole = principal.getRoles().contains("TEACHER") || principal.getRoles().contains("ASSISTANT");
        return hasGradingRole && canReview(assignment, principal);
    }

    private boolean canReview(Assignment assignment, UserPrincipal principal) {
        if (assignment == null || principal == null) {
            return false;
        }
        if (principal.getRoles().contains("ADMIN")) {
            return true;
        }
        Course course = courseService.getById(assignment.getCourseId());
        if (course != null && course.getOwnerId().equals(principal.getId())
                && principal.getRoles().contains("TEACHER")) {
            return true;
        }
        CourseMember member = courseMemberService.getByCourseIdAndUserId(assignment.getCourseId(), principal.getId());
        if (member == null || member.getStatus() == null || member.getStatus() != 1) {
            return false;
        }
        return ("TEACHER".equals(member.getMemberRole()) && principal.getRoles().contains("TEACHER"))
                || ("ASSISTANT".equals(member.getMemberRole()) && principal.getRoles().contains("ASSISTANT"));
    }

    private boolean canViewCourse(Course course, UserPrincipal principal) {
        if (principal == null || course == null) {
            return false;
        }
        if (principal.getRoles().contains("ADMIN")) {
            return true;
        }
        if (course.getOwnerId().equals(principal.getId()) && principal.getRoles().contains("TEACHER")) {
            return true;
        }
        CourseMember member = courseMemberService.getByCourseIdAndUserId(course.getId(), principal.getId());
        if (member == null || member.getStatus() == null || member.getStatus() != 1) {
            return false;
        }
        return ("TEACHER".equals(member.getMemberRole()) && principal.getRoles().contains("TEACHER"))
                || ("ASSISTANT".equals(member.getMemberRole()) && principal.getRoles().contains("ASSISTANT"));
    }

    private GradingItem toItem(Submission submission, Assignment assignment) {
        GradingItem item = new GradingItem();
        fillItem(item, submission, assignment);
        return item;
    }

    private GradingDetail toDetail(Submission submission, Assignment assignment) {
        GradingDetail detail = new GradingDetail();
        fillItem(detail, submission, assignment);
        detail.setContent(submission.getContent());
        List<FileRecordSummary> files = new ArrayList<FileRecordSummary>();
        for (FileRecord fileRecord : fileRecordService.listBySubmissionId(submission.getId())) {
            files.add(toFileSummary(fileRecord));
        }
        detail.setFiles(files);
        detail.setFileCount(files.size());
        return detail;
    }

    private void fillItem(GradingItem item, Submission submission, Assignment assignment) {
        Course course = assignment == null ? null : courseService.getById(assignment.getCourseId());
        User student = userService.getById(submission.getStudentId());
        Grade grade = gradeService.getBySubmissionId(submission.getId());
        User grader = grade == null || grade.getGraderId() == null ? null : userService.getById(grade.getGraderId());
        item.setSubmissionId(submission.getId());
        item.setAssignmentId(submission.getAssignmentId());
        item.setAssignmentTitle(assignment == null ? "" : assignment.getTitle());
        item.setAssignmentType(assignment == null ? "" : assignment.getAssignmentType());
        item.setCourseId(course == null ? null : course.getId());
        item.setCourseName(course == null ? "" : course.getCourseName());
        item.setStudentId(submission.getStudentId());
        item.setStudentUsername(student == null ? "" : student.getUsername());
        item.setStudentName(student == null ? "" : student.getRealName());
        item.setSubmitTime(submission.getSubmitTime());
        item.setLate(submission.getLate() != null && submission.getLate() == 1);
        item.setSubmissionStatus(submission.getStatus());
        item.setAutoScore(submission.getAutoScore());
        item.setFinalScore(submission.getFinalScore());
        boolean hasSimilarityAlert = assignment != null
                && duplicateRecordService.isSubmissionSuspectedDuplicate(assignment.getId(), submission.getId());
        item.setSuspectedDuplicate(hasSimilarityAlert);
        item.setHasSimilarityAlert(hasSimilarityAlert);
        item.setSimilarityScore(assignment == null ? null
                : duplicateRecordService.highestSimilarityScore(assignment.getId(), submission.getId()));
        item.setFileCount(fileRecordService.listBySubmissionId(submission.getId()).size());
        if (grade != null) {
            item.setGradeId(grade.getId());
            item.setGraderId(grade.getGraderId());
            item.setGraderName(grader == null ? "" : grader.getRealName());
            item.setGradeScore(grade.getScore());
            item.setComment(grade.getComment());
            item.setGradedAt(grade.getGradedAt());
            item.setGradeStatus(grade.getGradeStatus());
        } else {
            item.setGradeStatus("PENDING");
        }
    }

    private FileRecordSummary toFileSummary(FileRecord fileRecord) {
        FileRecordSummary summary = new FileRecordSummary();
        summary.setId(fileRecord.getId());
        summary.setSubmissionId(fileRecord.getSubmissionId());
        summary.setFileName(fileRecord.getFileName());
        summary.setFilePath(fileRecord.getFilePath());
        summary.setFileSize(fileRecord.getFileSize());
        summary.setFileHash(fileRecord.getFileHash());
        summary.setUploadedAt(fileRecord.getUploadedAt());
        return summary;
    }

    private Map<Long, Grade> loadGrades(List<Long> submissionIds) {
        Map<Long, Grade> gradesBySubmissionId = new HashMap<Long, Grade>();
        if (submissionIds.isEmpty()) {
            return gradesBySubmissionId;
        }
        List<Grade> grades = gradeService.list(new QueryWrapper<Grade>().in("submission_id", submissionIds));
        for (Grade grade : grades) {
            gradesBySubmissionId.put(grade.getSubmissionId(), grade);
        }
        return gradesBySubmissionId;
    }

    private BigDecimal calculateRate(int gradedCount, int submissionCount) {
        if (submissionCount <= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(gradedCount)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(submissionCount), 2, BigDecimal.ROUND_HALF_UP);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void ensureLogin(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(401, "请先登录");
        }
    }
}
