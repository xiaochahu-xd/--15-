package com.coursework.system.submission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.submission.dto.ObjectiveSubmissionRequest;
import com.coursework.system.submission.dto.StudentSubmissionAggregate;
import com.coursework.system.submission.dto.SubmissionDetail;
import com.coursework.system.submission.dto.SubmissionSummary;
import com.coursework.system.submission.dto.TextSubmissionRequest;
import com.coursework.system.submission.entity.Submission;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SubmissionService extends IService<Submission> {
    SubmissionDetail submitText(Long assignmentId, TextSubmissionRequest request, UserPrincipal principal, String ip);

    SubmissionDetail submitFile(Long assignmentId, MultipartFile file, String content, UserPrincipal principal, String ip);

    SubmissionDetail submitObjective(Long assignmentId, ObjectiveSubmissionRequest request, UserPrincipal principal, String ip);

    List<SubmissionSummary> listMySubmissions(Long assignmentId, UserPrincipal principal);

    List<SubmissionSummary> listAssignmentSubmissions(Long assignmentId, UserPrincipal principal);

    SubmissionDetail getSubmissionDetail(Long submissionId, UserPrincipal principal);

    List<StudentSubmissionAggregate> listStudentSubmissions(UserPrincipal principal, Long courseId,
                                                            String assignmentType, String status,
                                                            String keyword, Integer page, Integer size);
}
