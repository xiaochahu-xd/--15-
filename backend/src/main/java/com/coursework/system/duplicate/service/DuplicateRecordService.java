package com.coursework.system.duplicate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.duplicate.dto.DuplicateRecordSummary;
import com.coursework.system.duplicate.entity.DuplicateRecord;
import com.coursework.system.submission.entity.Submission;

import java.math.BigDecimal;
import java.util.List;

public interface DuplicateRecordService extends IService<DuplicateRecord> {
    void recordDuplicate(Long assignmentId, String fileHash, List<Long> submissionIds);

    DuplicateRecord recordSimilarity(Long assignmentId, Submission sourceSubmission, Submission matchedSubmission,
                                     BigDecimal similarityScore, BigDecimal threshold, String remark);

    boolean isSubmissionSuspectedDuplicate(Long assignmentId, Long submissionId);

    BigDecimal highestSimilarityScore(Long assignmentId, Long submissionId);

    List<DuplicateRecordSummary> listByAssignmentForReviewer(Long assignmentId, UserPrincipal principal);

    List<DuplicateRecordSummary> listBySubmissionForReviewer(Long submissionId, UserPrincipal principal);

    void confirm(Long recordId, UserPrincipal principal);

    void ignore(Long recordId, UserPrincipal principal);
}
