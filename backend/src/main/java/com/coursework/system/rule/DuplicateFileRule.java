package com.coursework.system.rule;

import com.coursework.system.submission.entity.FileRecord;
import com.coursework.system.submission.entity.Submission;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DuplicateFileRule implements Rule {

    @Override
    public String getName() {
        return "DuplicateFileRule";
    }

    @Override
    public boolean supports(RuleContext context) {
        return context.getAssignment() != null
                && "FILE".equals(context.getAssignment().getAssignmentType())
                && !context.getCurrentFileRecords().isEmpty();
    }

    @Override
    public RuleResult execute(RuleContext context) {
        RuleResult result = new RuleResult();
        Submission currentSubmission = context.getSubmission();
        Long currentSubmissionId = currentSubmission.getId();
        Map<Long, Submission> submissionsById = indexSubmissions(context.getAssignmentSubmissions());
        for (FileRecord current : context.getCurrentFileRecords()) {
            Set<Long> duplicateSubmissionIds = new LinkedHashSet<Long>();
            duplicateSubmissionIds.add(currentSubmissionId);
            for (FileRecord candidate : context.getAssignmentFileRecords()) {
                if (candidate.getSubmissionId().equals(currentSubmissionId)) {
                    continue;
                }
                Submission candidateSubmission = submissionsById.get(candidate.getSubmissionId());
                if (candidateSubmission == null
                        || currentSubmission.getStudentId().equals(candidateSubmission.getStudentId())) {
                    continue;
                }
                if (current.getFileHash() != null && current.getFileHash().equals(candidate.getFileHash())) {
                    duplicateSubmissionIds.add(candidate.getSubmissionId());
                }
            }
            if (duplicateSubmissionIds.size() > 1) {
                result.addDuplicate(current.getFileHash(), new ArrayList<Long>(duplicateSubmissionIds));
                result.addMessage("发现同一作业范围内相同文件 Hash：" + current.getFileHash());
            }
        }
        return result;
    }

    private Map<Long, Submission> indexSubmissions(List<Submission> submissions) {
        Map<Long, Submission> submissionsById = new HashMap<Long, Submission>();
        for (Submission submission : submissions) {
            if (submission.getId() != null) {
                submissionsById.put(submission.getId(), submission);
            }
        }
        return submissionsById;
    }
}
