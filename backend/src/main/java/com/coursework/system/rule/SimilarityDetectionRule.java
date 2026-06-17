package com.coursework.system.rule;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SimilarityDetectionRule implements Rule {
    private final SimilarityDetectionService similarityDetectionService;

    public SimilarityDetectionRule(SimilarityDetectionService similarityDetectionService) {
        this.similarityDetectionService = similarityDetectionService;
    }

    @Override
    public String getName() {
        return "SimilarityDetectionRule";
    }

    @Override
    public boolean supports(RuleContext context) {
        return context.getAssignment() != null
                && context.getSubmission() != null
                && ("TEXT".equals(context.getAssignment().getAssignmentType())
                || "FILE".equals(context.getAssignment().getAssignmentType()));
    }

    @Override
    public RuleResult execute(RuleContext context) {
        RuleResult result = new RuleResult();
        List<SimilarityMatchResult> matches = similarityDetectionService.detectAndRecord(context);
        if (!matches.isEmpty()) {
            result.setSuspectedDuplicate(true);
            for (SimilarityMatchResult match : matches) {
                result.addSimilarityMatch(match);
            }
            result.addMessage("发现高度相似提交，请教师核查");
        }
        return result;
    }
}
