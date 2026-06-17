package com.coursework.system.rule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.assignment.entity.Question;
import com.coursework.system.assignment.mapper.QuestionMapper;
import com.coursework.system.duplicate.service.DuplicateRecordService;
import com.coursework.system.submission.entity.Submission;
import com.coursework.system.submission.mapper.SubmissionMapper;
import com.coursework.system.submission.service.FileRecordService;
import com.coursework.system.submission.service.GradeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RuleEngine {
    private final List<Rule> rules;
    private final QuestionMapper questionMapper;
    private final FileRecordService fileRecordService;
    private final SubmissionMapper submissionMapper;
    private final GradeService gradeService;
    private final DuplicateRecordService duplicateRecordService;
    private final ObjectMapper objectMapper;

    public RuleEngine(List<Rule> rules,
                      QuestionMapper questionMapper,
                      FileRecordService fileRecordService,
                      SubmissionMapper submissionMapper,
                      GradeService gradeService,
                      DuplicateRecordService duplicateRecordService,
                      ObjectMapper objectMapper) {
        this.rules = rules;
        this.questionMapper = questionMapper;
        this.fileRecordService = fileRecordService;
        this.submissionMapper = submissionMapper;
        this.gradeService = gradeService;
        this.duplicateRecordService = duplicateRecordService;
        this.objectMapper = objectMapper;
    }

    public RuleResult evaluateAndApply(Assignment assignment, Submission submission) {
        RuleContext context = buildContext(assignment, submission);
        RuleResult result = executeRules(context);
        applyResult(assignment, submission, result);
        return result;
    }

    private RuleContext buildContext(Assignment assignment, Submission submission) {
        RuleContext context = new RuleContext();
        context.setAssignment(assignment);
        context.setSubmission(submission);
        if (assignment != null) {
            context.setQuestions(questionMapper.selectList(new QueryWrapper<Question>()
                    .eq("assignment_id", assignment.getId())
                    .orderByAsc("id")));
            context.setAssignmentFileRecords(fileRecordService.listByAssignmentId(assignment.getId()));
            context.setAssignmentSubmissions(submissionMapper.selectList(new QueryWrapper<Submission>()
                    .eq("assignment_id", assignment.getId())));
        }
        if (submission != null) {
            context.setStudentAnswers(parseAnswers(submission.getContent()));
            context.setCurrentFileRecords(fileRecordService.listBySubmissionId(submission.getId()));
        }
        return context;
    }

    private RuleResult executeRules(RuleContext context) {
        RuleResult result = new RuleResult();
        for (Rule rule : rules) {
            if (rule instanceof GradeLevelRule) {
                continue;
            }
            executeOneRule(rule, context, result);
        }
        if (result.getAutoScore() != null && context.getSubmission() != null) {
            context.getSubmission().setAutoScore(result.getAutoScore());
        }
        for (Rule rule : rules) {
            if (rule instanceof GradeLevelRule) {
                executeOneRule(rule, context, result);
            }
        }
        return result;
    }

    private void executeOneRule(Rule rule, RuleContext context, RuleResult result) {
        if (rule.supports(context)) {
            result.merge(rule.execute(context));
        }
    }

    private void applyResult(Assignment assignment, Submission submission, RuleResult result) {
        if (submission == null || result == null) {
            return;
        }
        if (result.getLate() != null) {
            submission.setLate(Boolean.TRUE.equals(result.getLate()) ? 1 : 0);
            submission.setStatus(Boolean.TRUE.equals(result.getLate()) ? "LATE" : "SUBMITTED");
        }
        if (result.getAutoScore() != null) {
            BigDecimal score = result.getAutoScore();
            submission.setAutoScore(score);
            submission.setFinalScore(score);
            gradeService.applyAutoScore(submission.getId(), score);
        }
        submissionMapper.updateById(submission);

        if (assignment != null && result.isSuspectedDuplicate()) {
            for (Map.Entry<String, List<Long>> entry : result.getDuplicateSubmissionIdsByHash().entrySet()) {
                duplicateRecordService.recordDuplicate(assignment.getId(), entry.getKey(), entry.getValue());
            }
        }
    }

    private Map<String, String> parseAnswers(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new HashMap<String, String>();
        }
        try {
            return objectMapper.readValue(content, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception ignored) {
            return new HashMap<String, String>();
        }
    }
}
