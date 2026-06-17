package com.coursework.system.submission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.assignment.service.AssignmentService;
import com.coursework.system.common.exception.BusinessException;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.course.entity.Course;
import com.coursework.system.course.entity.CourseMember;
import com.coursework.system.course.service.CourseMemberService;
import com.coursework.system.course.service.CourseService;
import com.coursework.system.duplicate.service.DuplicateRecordService;
import com.coursework.system.event.EventManager;
import com.coursework.system.event.SubmissionCreatedEvent;
import com.coursework.system.log.service.OperationLogService;
import com.coursework.system.rule.SubmissionRuleGateway;
import com.coursework.system.submission.dto.FileRecordSummary;
import com.coursework.system.submission.dto.ObjectiveSubmissionRequest;
import com.coursework.system.submission.dto.StudentSubmissionAggregate;
import com.coursework.system.submission.dto.SubmissionDetail;
import com.coursework.system.submission.dto.SubmissionSummary;
import com.coursework.system.submission.dto.TextSubmissionRequest;
import com.coursework.system.submission.entity.FileRecord;
import com.coursework.system.submission.entity.Grade;
import com.coursework.system.submission.entity.Submission;
import com.coursework.system.submission.mapper.SubmissionMapper;
import com.coursework.system.submission.service.FileRecordService;
import com.coursework.system.submission.service.FileStorageService;
import com.coursework.system.submission.service.GradeService;
import com.coursework.system.submission.service.SubmissionService;
import com.coursework.system.user.entity.User;
import com.coursework.system.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission> implements SubmissionService {
    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final CourseMemberService courseMemberService;
    private final UserService userService;
    private final FileRecordService fileRecordService;
    private final FileStorageService fileStorageService;
    private final GradeService gradeService;
    private final DuplicateRecordService duplicateRecordService;
    private final EventManager eventManager;
    private final SubmissionRuleGateway submissionRuleGateway;
    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    public SubmissionServiceImpl(AssignmentService assignmentService,
                                 CourseService courseService,
                                 CourseMemberService courseMemberService,
                                 UserService userService,
                                 FileRecordService fileRecordService,
                                 FileStorageService fileStorageService,
                                 GradeService gradeService,
                                 DuplicateRecordService duplicateRecordService,
                                 EventManager eventManager,
                                 SubmissionRuleGateway submissionRuleGateway,
                                 OperationLogService operationLogService,
                                 ObjectMapper objectMapper) {
        this.assignmentService = assignmentService;
        this.courseService = courseService;
        this.courseMemberService = courseMemberService;
        this.userService = userService;
        this.fileRecordService = fileRecordService;
        this.fileStorageService = fileStorageService;
        this.gradeService = gradeService;
        this.duplicateRecordService = duplicateRecordService;
        this.eventManager = eventManager;
        this.submissionRuleGateway = submissionRuleGateway;
        this.operationLogService = operationLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public SubmissionDetail submitText(Long assignmentId, TextSubmissionRequest request, UserPrincipal principal, String ip) {
        Assignment assignment = requireStudentSubmissionPermission(assignmentId, principal);
        ensureAssignmentType(assignment, "TEXT");
        Submission submission = createBaseSubmission(assignment, principal.getId(), request.getContent().trim());
        submissionRuleGateway.afterSubmissionCreated(assignment, submission, principal, ip);
        publishSubmissionCreated(assignment, submission, principal, ip);
        operationLogService.record(principal.getId(), principal.getUsername(), "SUBMISSION_TEXT_CREATE",
                "ASSIGNMENT", assignmentId, ip, "SUCCESS", "提交文本作业");
        return toDetail(submission);
    }

    @Override
    @Transactional
    public SubmissionDetail submitFile(Long assignmentId, MultipartFile file, String content, UserPrincipal principal, String ip) {
        Assignment assignment = requireStudentSubmissionPermission(assignmentId, principal);
        ensureAssignmentType(assignment, "FILE");
        Submission submission = createBaseSubmission(assignment, principal.getId(), content);
        FileRecord fileRecord = fileStorageService.store(submission.getId(), assignmentId, file);
        fileRecordService.save(fileRecord);
        submissionRuleGateway.afterSubmissionCreated(assignment, submission, principal, ip);
        publishSubmissionCreated(assignment, submission, principal, ip);
        operationLogService.record(principal.getId(), principal.getUsername(), "SUBMISSION_FILE_CREATE",
                "ASSIGNMENT", assignmentId, ip, "SUCCESS", "提交文件作业：" + fileRecord.getFileName());
        return toDetail(submission);
    }

    @Override
    @Transactional
    public SubmissionDetail submitObjective(Long assignmentId, ObjectiveSubmissionRequest request, UserPrincipal principal, String ip) {
        Assignment assignment = requireStudentSubmissionPermission(assignmentId, principal);
        if (!"SINGLE_CHOICE".equals(assignment.getAssignmentType()) && !"TRUE_FALSE".equals(assignment.getAssignmentType())) {
            throw new BusinessException(400, "该作业不是客观题作业");
        }
        String content;
        try {
            content = objectMapper.writeValueAsString(request.getAnswers());
        } catch (Exception exception) {
            throw new BusinessException(500, "客观题答案保存失败：" + exception.getMessage());
        }
        Submission submission = createBaseSubmission(assignment, principal.getId(), content);
        submissionRuleGateway.afterSubmissionCreated(assignment, submission, principal, ip);
        publishSubmissionCreated(assignment, submission, principal, ip);
        operationLogService.record(principal.getId(), principal.getUsername(), "SUBMISSION_OBJECTIVE_CREATE",
                "ASSIGNMENT", assignmentId, ip, "SUCCESS", "提交客观题答案");
        return toDetail(submission);
    }

    @Override
    public List<SubmissionSummary> listMySubmissions(Long assignmentId, UserPrincipal principal) {
        Assignment assignment = requireStudentViewMyPermission(assignmentId, principal);
        List<Submission> submissions = list(new QueryWrapper<Submission>()
                .eq("assignment_id", assignment.getId())
                .eq("student_id", principal.getId())
                .orderByDesc("submit_time"));
        return toSummaries(submissions);
    }

    @Override
    public List<SubmissionSummary> listAssignmentSubmissions(Long assignmentId, UserPrincipal principal) {
        ensureLogin(principal);
        Assignment assignment = assignmentService.getActiveAssignment(assignmentId);
        if (!canReviewSubmissions(assignment, principal)) {
            recordDenied(principal, "SUBMISSION_LIST", assignmentId, "无权查看该作业提交");
            throw new BusinessException(403, "只有课程教师或助教可以查看该作业提交");
        }
        List<Submission> submissions = list(new QueryWrapper<Submission>()
                .eq("assignment_id", assignmentId)
                .orderByDesc("submit_time"));
        return toSummaries(submissions);
    }

    @Override
    public SubmissionDetail getSubmissionDetail(Long submissionId, UserPrincipal principal) {
        ensureLogin(principal);
        Submission submission = getById(submissionId);
        if (submission == null) {
            throw new BusinessException(404, "提交记录不存在");
        }
        Assignment assignment = assignmentService.getActiveAssignment(submission.getAssignmentId());
        if (principal.getRoles().contains("STUDENT")) {
            if (!submission.getStudentId().equals(principal.getId())) {
                throw new BusinessException(403, "学生只能查看自己的提交记录");
            }
            requireStudentViewMyPermission(assignment.getId(), principal);
            return toDetail(submission);
        }
        if (!canReviewSubmissions(assignment, principal)) {
            throw new BusinessException(403, "无权查看该提交记录");
        }
        return toDetail(submission);
    }

    @Override
    public List<StudentSubmissionAggregate> listStudentSubmissions(UserPrincipal principal, Long courseId,
                                                                   String assignmentType, String status,
                                                                   String keyword, Integer page, Integer size) {
        ensureLogin(principal);
        if (!principal.getRoles().contains("STUDENT")) {
            throw new BusinessException(403, "Only students can query their own submission aggregation");
        }
        if (courseId != null && !courseService.canViewCourse(courseId, principal)) {
            throw new BusinessException(403, "No permission to view this course submissions");
        }
        List<Submission> submissions = list(new QueryWrapper<Submission>()
                .eq("student_id", principal.getId())
                .orderByDesc("submit_time"));
        List<StudentSubmissionAggregate> aggregates = new ArrayList<StudentSubmissionAggregate>();
        for (Submission submission : submissions) {
            StudentSubmissionAggregate aggregate = toStudentSubmissionAggregate(submission);
            if (aggregate == null) {
                continue;
            }
            if (matchesStudentAggregateFilters(aggregate, courseId, assignmentType, status, keyword)) {
                aggregates.add(aggregate);
            }
        }
        return paginate(aggregates, page, size);
    }

    private Submission createBaseSubmission(Assignment assignment, Long studentId, String content) {
        Submission submission = new Submission();
        submission.setAssignmentId(assignment.getId());
        submission.setStudentId(studentId);
        submission.setContent(content);
        submission.setSubmitTime(LocalDateTime.now());
        submission.setLate(0);
        submission.setStatus("SUBMITTED");
        save(submission);
        gradeService.createPendingGrade(submission.getId());
        return submission;
    }

    private void publishSubmissionCreated(Assignment assignment, Submission submission,
                                          UserPrincipal principal, String ip) {
        eventManager.publish(new SubmissionCreatedEvent(submission.getId(), assignment.getId(),
                assignment.getTitle(), principal.getId(), principal.getUsername(), ip));
    }

    private Assignment requireStudentSubmissionPermission(Long assignmentId, UserPrincipal principal) {
        ensureLogin(principal);
        if (!principal.getRoles().contains("STUDENT")) {
            throw new BusinessException(403, "只有学生可以提交作业");
        }
        Assignment assignment = assignmentService.getActiveAssignment(assignmentId);
        CourseMember member = courseMemberService.getByCourseIdAndUserId(assignment.getCourseId(), principal.getId());
        if (member == null || member.getStatus() == null || member.getStatus() != 1 || !"STUDENT".equals(member.getMemberRole())) {
            throw new BusinessException(403, "只有课程学生可以提交该作业");
        }
        return assignment;
    }

    private Assignment requireStudentViewMyPermission(Long assignmentId, UserPrincipal principal) {
        Assignment assignment = requireStudentSubmissionPermission(assignmentId, principal);
        if (!assignmentService.canViewAssignment(assignment, principal)) {
            throw new BusinessException(403, "无权查看该作业");
        }
        return assignment;
    }

    private boolean canReviewSubmissions(Assignment assignment, UserPrincipal principal) {
        if (principal == null || assignment == null) {
            return false;
        }
        if (principal.getRoles().contains("ADMIN")) {
            return true;
        }
        Course course = courseService.getById(assignment.getCourseId());
        if (course != null && course.getOwnerId().equals(principal.getId()) && principal.getRoles().contains("TEACHER")) {
            return true;
        }
        CourseMember member = courseMemberService.getByCourseIdAndUserId(assignment.getCourseId(), principal.getId());
        if (member == null || member.getStatus() == null || member.getStatus() != 1) {
            return false;
        }
        return ("TEACHER".equals(member.getMemberRole()) && principal.getRoles().contains("TEACHER"))
                || ("ASSISTANT".equals(member.getMemberRole()) && principal.getRoles().contains("ASSISTANT"));
    }

    private void ensureAssignmentType(Assignment assignment, String expectedType) {
        if (!expectedType.equals(assignment.getAssignmentType())) {
            throw new BusinessException(400, "提交接口与作业类型不匹配");
        }
    }

    private List<SubmissionSummary> toSummaries(List<Submission> submissions) {
        List<SubmissionSummary> summaries = new ArrayList<SubmissionSummary>();
        for (Submission submission : submissions) {
            summaries.add(toSummary(submission));
        }
        return summaries;
    }

    private SubmissionDetail toDetail(Submission submission) {
        SubmissionDetail detail = new SubmissionDetail();
        fillSummary(detail, submission);
        List<FileRecordSummary> files = new ArrayList<FileRecordSummary>();
        for (FileRecord fileRecord : fileRecordService.listBySubmissionId(submission.getId())) {
            files.add(toFileSummary(fileRecord));
        }
        detail.setFiles(files);
        detail.setFileCount(files.size());
        return detail;
    }

    private SubmissionSummary toSummary(Submission submission) {
        SubmissionSummary summary = new SubmissionSummary();
        fillSummary(summary, submission);
        summary.setFileCount(fileRecordService.listBySubmissionId(submission.getId()).size());
        return summary;
    }

    private void fillSummary(SubmissionSummary summary, Submission submission) {
        Assignment assignment = assignmentService.getById(submission.getAssignmentId());
        Course course = assignment == null ? null : courseService.getById(assignment.getCourseId());
        User student = userService.getById(submission.getStudentId());
        summary.setId(submission.getId());
        summary.setAssignmentId(submission.getAssignmentId());
        summary.setAssignmentTitle(assignment == null ? "" : assignment.getTitle());
        summary.setAssignmentType(assignment == null ? "" : assignment.getAssignmentType());
        summary.setCourseId(course == null ? null : course.getId());
        summary.setCourseName(course == null ? "" : course.getCourseName());
        summary.setStudentId(submission.getStudentId());
        summary.setStudentUsername(student == null ? "" : student.getUsername());
        summary.setStudentName(student == null ? "" : student.getRealName());
        summary.setContent(submission.getContent());
        summary.setSubmitTime(submission.getSubmitTime());
        summary.setLate(submission.getLate() != null && submission.getLate() == 1);
        summary.setStatus(submission.getStatus());
        summary.setAutoScore(submission.getAutoScore());
        summary.setFinalScore(submission.getFinalScore());
        boolean hasSimilarityAlert = assignment != null
                && duplicateRecordService.isSubmissionSuspectedDuplicate(assignment.getId(), submission.getId());
        summary.setSuspectedDuplicate(hasSimilarityAlert);
        summary.setHasSimilarityAlert(hasSimilarityAlert);
        summary.setSimilarityScore(assignment == null ? null
                : duplicateRecordService.highestSimilarityScore(assignment.getId(), submission.getId()));
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

    private StudentSubmissionAggregate toStudentSubmissionAggregate(Submission submission) {
        Assignment assignment = assignmentService.getById(submission.getAssignmentId());
        if (assignment == null) {
            return null;
        }
        Course course = courseService.getById(assignment.getCourseId());
        Grade grade = gradeService.getBySubmissionId(submission.getId());
        List<FileRecord> files = fileRecordService.listBySubmissionId(submission.getId());
        FileRecord firstFile = files.isEmpty() ? null : files.get(0);

        StudentSubmissionAggregate aggregate = new StudentSubmissionAggregate();
        aggregate.setSubmissionId(submission.getId());
        aggregate.setAssignmentId(assignment.getId());
        aggregate.setAssignmentTitle(assignment.getTitle());
        aggregate.setAssignmentType(assignment.getAssignmentType());
        aggregate.setCourseId(assignment.getCourseId());
        aggregate.setCourseName(course == null ? "" : course.getCourseName());
        aggregate.setSubmitTime(submission.getSubmitTime());
        aggregate.setDeadline(assignment.getDeadline());
        aggregate.setLate(submission.getLate() != null && submission.getLate() == 1);
        boolean hasSimilarityAlert = duplicateRecordService.isSubmissionSuspectedDuplicate(assignment.getId(), submission.getId());
        aggregate.setDuplicate(hasSimilarityAlert);
        aggregate.setSimilarityAlert(hasSimilarityAlert);
        aggregate.setSimilarityScore(duplicateRecordService.highestSimilarityScore(assignment.getId(), submission.getId()));
        aggregate.setAutoScore(submission.getAutoScore());
        aggregate.setFinalScore(submission.getFinalScore());
        aggregate.setGradeStatus(grade == null ? "PENDING" : grade.getGradeStatus());
        aggregate.setComment(grade == null ? null : grade.getComment());
        aggregate.setFileName(firstFile == null ? null : firstFile.getFileName());
        aggregate.setFileHash(firstFile == null ? null : firstFile.getFileHash());
        aggregate.setStatus(submission.getStatus());
        return aggregate;
    }

    private boolean matchesStudentAggregateFilters(StudentSubmissionAggregate aggregate, Long courseId,
                                                   String assignmentType, String status, String keyword) {
        if (courseId != null && !courseId.equals(aggregate.getCourseId())) {
            return false;
        }
        String normalizedType = normalize(assignmentType);
        if (!normalizedType.isEmpty() && !"ALL".equals(normalizedType)) {
            boolean objective = "OBJECTIVE".equals(normalizedType)
                    && ("SINGLE_CHOICE".equals(aggregate.getAssignmentType())
                    || "TRUE_FALSE".equals(aggregate.getAssignmentType()));
            if (!objective && !normalizedType.equals(aggregate.getAssignmentType())) {
                return false;
            }
        }
        String normalizedStatus = normalize(status);
        if (!normalizedStatus.isEmpty() && !"ALL".equals(normalizedStatus)) {
            if ("LATE".equals(normalizedStatus) && !Boolean.TRUE.equals(aggregate.getLate())) {
                return false;
            } else if ("GRADED".equals(normalizedStatus)
                    && !"GRADED".equals(aggregate.getGradeStatus())
                    && aggregate.getFinalScore() == null) {
                return false;
            } else if ("DUPLICATE".equals(normalizedStatus) && !Boolean.TRUE.equals(aggregate.getDuplicate())) {
                return false;
            } else if (!"LATE".equals(normalizedStatus)
                    && !"GRADED".equals(normalizedStatus)
                    && !"DUPLICATE".equals(normalizedStatus)
                    && !normalizedStatus.equals(aggregate.getStatus())) {
                return false;
            }
        }
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        if (!normalizedKeyword.isEmpty()) {
            String haystack = (safe(aggregate.getCourseName()) + " "
                    + safe(aggregate.getAssignmentTitle()) + " "
                    + safe(aggregate.getFileName())).toLowerCase();
            return haystack.contains(normalizedKeyword);
        }
        return true;
    }

    private List<StudentSubmissionAggregate> paginate(List<StudentSubmissionAggregate> items, Integer page, Integer size) {
        if (page == null || size == null || page <= 0 || size <= 0) {
            return items;
        }
        int fromIndex = (page - 1) * size;
        if (fromIndex >= items.size()) {
            return new ArrayList<StudentSubmissionAggregate>();
        }
        int toIndex = Math.min(fromIndex + size, items.size());
        return new ArrayList<StudentSubmissionAggregate>(items.subList(fromIndex, toIndex));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void ensureLogin(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(401, "请先登录");
        }
    }

    private void recordDenied(UserPrincipal principal, String operation, Long targetId, String detail) {
        operationLogService.record(principal.getId(), principal.getUsername(), operation,
                "SUBMISSION", targetId, "", "DENIED", detail);
    }
}
