package com.coursework.system.duplicate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.assignment.service.AssignmentService;
import com.coursework.system.common.exception.BusinessException;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.course.dto.CourseMemberSummary;
import com.coursework.system.course.entity.Course;
import com.coursework.system.course.entity.CourseMember;
import com.coursework.system.course.service.CourseMemberService;
import com.coursework.system.course.service.CourseService;
import com.coursework.system.duplicate.dto.DuplicateRecordSummary;
import com.coursework.system.duplicate.entity.DuplicateRecord;
import com.coursework.system.duplicate.mapper.DuplicateRecordMapper;
import com.coursework.system.duplicate.service.DuplicateRecordService;
import com.coursework.system.notification.service.NotificationService;
import com.coursework.system.submission.entity.Submission;
import com.coursework.system.submission.mapper.SubmissionMapper;
import com.coursework.system.user.entity.User;
import com.coursework.system.user.service.UserService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class DuplicateRecordServiceImpl extends ServiceImpl<DuplicateRecordMapper, DuplicateRecord>
        implements DuplicateRecordService {
    private static final String TYPE_EXACT_HASH = "EXACT_HASH";
    private static final String TYPE_TEXT_SIMILARITY = "TEXT_SIMILARITY";
    private static final String STATUS_PENDING = "PENDING_REVIEW";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_IGNORED = "IGNORED";
    private static final BigDecimal DEFAULT_THRESHOLD = new BigDecimal("0.9000");

    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final CourseMemberService courseMemberService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final SubmissionMapper submissionMapper;

    public DuplicateRecordServiceImpl(AssignmentService assignmentService,
                                      CourseService courseService,
                                      CourseMemberService courseMemberService,
                                      NotificationService notificationService,
                                      UserService userService,
                                      SubmissionMapper submissionMapper) {
        this.assignmentService = assignmentService;
        this.courseService = courseService;
        this.courseMemberService = courseMemberService;
        this.notificationService = notificationService;
        this.userService = userService;
        this.submissionMapper = submissionMapper;
    }

    @Override
    public void recordDuplicate(Long assignmentId, String fileHash, List<Long> submissionIds) {
        if (assignmentId == null || fileHash == null || fileHash.trim().isEmpty() || submissionIds == null) {
            return;
        }
        List<Long> normalizedSubmissionIds = normalizeSubmissionIds(submissionIds);
        if (normalizedSubmissionIds.size() < 2) {
            return;
        }

        DuplicateRecord record = getOne(new QueryWrapper<DuplicateRecord>()
                .eq("assignment_id", assignmentId)
                .eq("file_hash", fileHash), false);
        boolean created = false;
        if (record == null) {
            record = new DuplicateRecord();
            record.setAssignmentId(assignmentId);
            record.setFileHash(fileHash);
            record.setDetectionType(TYPE_EXACT_HASH);
            record.setThreshold(DEFAULT_THRESHOLD);
            record.setStatus(STATUS_PENDING);
            created = true;
        }
        String oldSubmissionIds = record.getSubmissionIds();
        String newSubmissionIds = joinSubmissionIds(normalizedSubmissionIds);
        record.setSubmissionIds(newSubmissionIds);
        record.setDetectedAt(LocalDateTime.now());
        record.setRemark("文件 Hash 完全相同，作为辅助检测结果。");
        saveOrUpdate(record);
        if (created || oldSubmissionIds == null || !oldSubmissionIds.equals(newSubmissionIds)) {
            notifyReviewers(record);
        }
    }

    @Override
    public DuplicateRecord recordSimilarity(Long assignmentId, Submission sourceSubmission, Submission matchedSubmission,
                                            BigDecimal similarityScore, BigDecimal threshold, String remark) {
        if (assignmentId == null || sourceSubmission == null || matchedSubmission == null || similarityScore == null) {
            return null;
        }
        if (sourceSubmission.getStudentId() == null || sourceSubmission.getStudentId().equals(matchedSubmission.getStudentId())) {
            return null;
        }
        BigDecimal normalizedThreshold = threshold == null ? DEFAULT_THRESHOLD : threshold.setScale(4, RoundingMode.HALF_UP);
        BigDecimal normalizedScore = similarityScore.setScale(4, RoundingMode.HALF_UP);
        String recordKey = "SIM-" + sourceSubmission.getId() + "-" + matchedSubmission.getId();

        DuplicateRecord record = getOne(new QueryWrapper<DuplicateRecord>()
                .eq("assignment_id", assignmentId)
                .eq("source_submission_id", sourceSubmission.getId())
                .eq("matched_submission_id", matchedSubmission.getId())
                .eq("detection_type", TYPE_TEXT_SIMILARITY), false);
        boolean shouldNotify = false;
        if (record == null) {
            record = new DuplicateRecord();
            record.setAssignmentId(assignmentId);
            record.setFileHash(recordKey);
            record.setSourceSubmissionId(sourceSubmission.getId());
            record.setMatchedSubmissionId(matchedSubmission.getId());
            record.setSourceStudentId(sourceSubmission.getStudentId());
            record.setMatchedStudentId(matchedSubmission.getStudentId());
            record.setDetectionType(TYPE_TEXT_SIMILARITY);
            record.setStatus(STATUS_PENDING);
            shouldNotify = true;
        }
        record.setSubmissionIds(joinSubmissionIds(normalizeSubmissionIds(toList(sourceSubmission.getId(), matchedSubmission.getId()))));
        record.setSimilarityScore(normalizedScore);
        record.setThreshold(normalizedThreshold);
        record.setDetectedAt(LocalDateTime.now());
        record.setRemark(remark == null ? "文本相似度达到核查阈值。" : remark);
        saveOrUpdate(record);
        if (shouldNotify) {
            notifyReviewers(record);
        }
        return record;
    }

    @Override
    public boolean isSubmissionSuspectedDuplicate(Long assignmentId, Long submissionId) {
        if (assignmentId == null || submissionId == null) {
            return false;
        }
        List<DuplicateRecord> records = list(new QueryWrapper<DuplicateRecord>()
                .eq("assignment_id", assignmentId)
                .in("status", STATUS_PENDING, STATUS_CONFIRMED, "SUSPECTED"));
        for (DuplicateRecord record : records) {
            if (submissionMatches(record, submissionId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BigDecimal highestSimilarityScore(Long assignmentId, Long submissionId) {
        if (assignmentId == null || submissionId == null) {
            return null;
        }
        BigDecimal highest = null;
        List<DuplicateRecord> records = list(new QueryWrapper<DuplicateRecord>()
                .eq("assignment_id", assignmentId)
                .eq("detection_type", TYPE_TEXT_SIMILARITY)
                .in("status", STATUS_PENDING, STATUS_CONFIRMED, "SUSPECTED"));
        for (DuplicateRecord record : records) {
            if (submissionMatches(record, submissionId) && record.getSimilarityScore() != null) {
                if (highest == null || record.getSimilarityScore().compareTo(highest) > 0) {
                    highest = record.getSimilarityScore();
                }
            }
        }
        return highest;
    }

    @Override
    public List<DuplicateRecordSummary> listByAssignmentForReviewer(Long assignmentId, UserPrincipal principal) {
        ensureLogin(principal);
        Assignment assignment = assignmentService.getActiveAssignment(assignmentId);
        if (!canReview(assignment, principal)) {
            throw new BusinessException(403, "只有课程教师、助教或管理员可以查看相似度核查记录");
        }
        List<DuplicateRecord> records = list(new QueryWrapper<DuplicateRecord>()
                .eq("assignment_id", assignmentId)
                .orderByDesc("detected_at"));
        List<DuplicateRecordSummary> summaries = new ArrayList<DuplicateRecordSummary>();
        for (DuplicateRecord record : records) {
            summaries.add(toSummary(record, assignment));
        }
        return summaries;
    }

    @Override
    public List<DuplicateRecordSummary> listBySubmissionForReviewer(Long submissionId, UserPrincipal principal) {
        ensureLogin(principal);
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(404, "提交记录不存在");
        }
        Assignment assignment = assignmentService.getActiveAssignment(submission.getAssignmentId());
        if (!canReview(assignment, principal)) {
            throw new BusinessException(403, "无权查看该提交的相似度核查记录");
        }
        List<DuplicateRecord> records = list(new QueryWrapper<DuplicateRecord>()
                .eq("assignment_id", assignment.getId())
                .orderByDesc("detected_at"));
        List<DuplicateRecordSummary> summaries = new ArrayList<DuplicateRecordSummary>();
        for (DuplicateRecord record : records) {
            if (submissionMatches(record, submissionId)) {
                summaries.add(toSummary(record, assignment));
            }
        }
        return summaries;
    }

    @Override
    public void confirm(Long recordId, UserPrincipal principal) {
        updateStatus(recordId, principal, STATUS_CONFIRMED);
    }

    @Override
    public void ignore(Long recordId, UserPrincipal principal) {
        updateStatus(recordId, principal, STATUS_IGNORED);
    }

    private void updateStatus(Long recordId, UserPrincipal principal, String status) {
        ensureLogin(principal);
        DuplicateRecord record = getById(recordId);
        if (record == null) {
            throw new BusinessException(404, "相似度核查记录不存在");
        }
        Assignment assignment = assignmentService.getActiveAssignment(record.getAssignmentId());
        if (!canReview(assignment, principal)) {
            throw new BusinessException(403, "无权处理该相似度核查记录");
        }
        record.setStatus(status);
        saveOrUpdate(record);
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

    private DuplicateRecordSummary toSummary(DuplicateRecord record, Assignment assignment) {
        DuplicateRecordSummary summary = new DuplicateRecordSummary();
        summary.setId(record.getId());
        summary.setAssignmentId(record.getAssignmentId());
        summary.setAssignmentTitle(assignment == null ? "" : assignment.getTitle());
        summary.setFileHash(record.getFileHash());
        summary.setSubmissionIds(record.getSubmissionIds());
        summary.setSubmissionIdList(parseSubmissionIds(record.getSubmissionIds()));
        summary.setSourceSubmissionId(record.getSourceSubmissionId());
        summary.setMatchedSubmissionId(record.getMatchedSubmissionId());
        summary.setSourceStudentId(record.getSourceStudentId());
        summary.setMatchedStudentId(record.getMatchedStudentId());
        summary.setSourceStudentName(userRealName(record.getSourceStudentId()));
        summary.setMatchedStudentName(userRealName(record.getMatchedStudentId()));
        summary.setDetectionType(record.getDetectionType() == null ? TYPE_EXACT_HASH : record.getDetectionType());
        summary.setSimilarityScore(record.getSimilarityScore());
        summary.setThreshold(record.getThreshold());
        summary.setDetectedAt(record.getDetectedAt());
        summary.setStatus(record.getStatus());
        summary.setRemark(record.getRemark());
        return summary;
    }

    private String userRealName(Long userId) {
        if (userId == null) {
            return "";
        }
        User user = userService.getById(userId);
        return user == null ? "" : user.getRealName();
    }

    private boolean submissionMatches(DuplicateRecord record, Long submissionId) {
        if (submissionId == null || record == null || STATUS_IGNORED.equals(record.getStatus())) {
            return false;
        }
        if (submissionId.equals(record.getSourceSubmissionId()) || submissionId.equals(record.getMatchedSubmissionId())) {
            return true;
        }
        return parseSubmissionIds(record.getSubmissionIds()).contains(submissionId);
    }

    private List<Long> normalizeSubmissionIds(List<Long> submissionIds) {
        Set<Long> uniqueIds = new LinkedHashSet<Long>();
        for (Long submissionId : submissionIds) {
            if (submissionId != null) {
                uniqueIds.add(submissionId);
            }
        }
        List<Long> normalizedIds = new ArrayList<Long>(uniqueIds);
        Collections.sort(normalizedIds);
        return normalizedIds;
    }

    private List<Long> toList(Long first, Long second) {
        List<Long> values = new ArrayList<Long>();
        values.add(first);
        values.add(second);
        return values;
    }

    private String joinSubmissionIds(List<Long> submissionIds) {
        StringBuilder builder = new StringBuilder();
        for (Long submissionId : submissionIds) {
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(submissionId);
        }
        return builder.toString();
    }

    private List<Long> parseSubmissionIds(String submissionIds) {
        List<Long> values = new ArrayList<Long>();
        if (submissionIds == null || submissionIds.trim().isEmpty()) {
            return values;
        }
        String[] parts = submissionIds.split(",");
        for (String part : parts) {
            try {
                values.add(Long.valueOf(part.trim()));
            } catch (NumberFormatException ignored) {
                // Ignore malformed legacy values while keeping the page usable.
            }
        }
        return values;
    }

    private void notifyReviewers(DuplicateRecord record) {
        try {
            Assignment assignment = assignmentService.getById(record.getAssignmentId());
            if (assignment == null) {
                return;
            }
            Course course = courseService.getById(assignment.getCourseId());
            Set<Long> receiverIds = new LinkedHashSet<Long>();
            if (course != null && course.getOwnerId() != null) {
                receiverIds.add(course.getOwnerId());
            }
            for (CourseMemberSummary member : courseMemberService.listActiveMembers(assignment.getCourseId())) {
                if ("TEACHER".equals(member.getMemberRole()) || "ASSISTANT".equals(member.getMemberRole())) {
                    receiverIds.add(member.getUserId());
                }
            }
            for (Long receiverId : receiverIds) {
                notificationService.create(receiverId, notificationType(record),
                        notificationTitle(record),
                        notificationContent(record, assignment),
                        "DUPLICATE_RECORD", record.getId(), assignment.getCourseId());
            }
        } catch (RuntimeException ignored) {
            // Notification failure must not block the similarity record or student submission flow.
        }
    }

    private String notificationType(DuplicateRecord record) {
        return TYPE_TEXT_SIMILARITY.equals(record.getDetectionType()) ? "SIMILARITY_ALERT" : "DUPLICATE_RECORD";
    }

    private String notificationTitle(DuplicateRecord record) {
        return TYPE_TEXT_SIMILARITY.equals(record.getDetectionType()) ? "发现高度相似提交" : "发现完全相同文件";
    }

    private String notificationContent(DuplicateRecord record, Assignment assignment) {
        if (TYPE_TEXT_SIMILARITY.equals(record.getDetectionType())) {
            return "系统检测到作业《" + assignment.getTitle() + "》中，学生"
                    + userRealName(record.getSourceStudentId()) + "与学生"
                    + userRealName(record.getMatchedStudentId()) + "的提交内容相似度为"
                    + percent(record.getSimilarityScore()) + "，请前往待批改列表核查。";
        }
        return "作业《" + assignment.getTitle() + "》中存在文件 Hash 完全相同的提交，请前往相似度核查页面核查。";
    }

    private String percent(BigDecimal value) {
        if (value == null) {
            return "-";
        }
        return value.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private void ensureLogin(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(401, "请先登录");
        }
    }
}
