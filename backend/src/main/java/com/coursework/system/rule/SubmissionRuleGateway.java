package com.coursework.system.rule;

import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.submission.entity.Submission;

public interface SubmissionRuleGateway {
    void afterSubmissionCreated(Assignment assignment, Submission submission, UserPrincipal principal, String ip);
}
