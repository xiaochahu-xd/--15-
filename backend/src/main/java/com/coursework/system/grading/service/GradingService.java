package com.coursework.system.grading.service;

import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.grading.dto.GradeRequest;
import com.coursework.system.grading.dto.GradingDetail;
import com.coursework.system.grading.dto.GradingItem;
import com.coursework.system.grading.dto.GradingProgress;
import com.coursework.system.grading.dto.ReturnSubmissionRequest;

import java.util.List;

public interface GradingService {
    List<GradingItem> listGradingList(Long assignmentId, UserPrincipal principal);

    GradingDetail getSubmissionDetail(Long submissionId, UserPrincipal principal);

    GradingDetail saveGrade(Long submissionId, GradeRequest request, UserPrincipal principal, String ip);

    GradingDetail returnSubmission(Long submissionId, ReturnSubmissionRequest request, UserPrincipal principal, String ip);

    GradingDetail getGrade(Long submissionId, UserPrincipal principal);

    GradingProgress getCourseProgress(Long courseId, UserPrincipal principal);
}
