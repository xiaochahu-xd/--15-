package com.coursework.system.assignment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursework.system.assignment.dto.QuestionCreateRequest;
import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.assignment.entity.Question;
import com.coursework.system.assignment.mapper.QuestionMapper;
import com.coursework.system.assignment.service.AssignmentService;
import com.coursework.system.assignment.service.QuestionService;
import com.coursework.system.common.exception.BusinessException;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.log.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
    private static final Set<String> SUPPORTED_QUESTION_TYPES =
            new HashSet<String>(Arrays.asList("SINGLE_CHOICE", "TRUE_FALSE"));

    private final AssignmentService assignmentService;
    private final OperationLogService operationLogService;

    public QuestionServiceImpl(AssignmentService assignmentService, OperationLogService operationLogService) {
        this.assignmentService = assignmentService;
        this.operationLogService = operationLogService;
    }

    @Override
    public Question addQuestion(Long assignmentId, QuestionCreateRequest request, UserPrincipal principal, String ip) {
        ensureLogin(principal);
        Assignment assignment = assignmentService.getActiveAssignment(assignmentId);
        if (!assignmentService.canEditAssignment(assignment, principal)) {
            operationLogService.record(principal.getId(), principal.getUsername(), "QUESTION_CREATE",
                    "ASSIGNMENT", assignmentId, ip, "DENIED", "无权添加客观题");
            throw new BusinessException(403, "只有课程负责人或课程教师可以添加客观题");
        }
        String questionType = normalize(request.getQuestionType());
        validateQuestion(questionType, request, assignment);
        Question question = new Question();
        question.setAssignmentId(assignmentId);
        question.setQuestionType(questionType);
        question.setContent(request.getContent().trim());
        question.setOptions(request.getOptions());
        question.setStandardAnswer(normalizeAnswer(questionType, request.getStandardAnswer()));
        question.setScore(request.getScore());
        save(question);
        operationLogService.record(principal.getId(), principal.getUsername(), "QUESTION_CREATE",
                "ASSIGNMENT", assignmentId, ip, "SUCCESS", "添加客观题：" + questionType);
        return question;
    }

    @Override
    public List<Question> listQuestions(Long assignmentId, UserPrincipal principal) {
        ensureLogin(principal);
        Assignment assignment = assignmentService.getActiveAssignment(assignmentId);
        if (!assignmentService.canViewAssignment(assignment, principal)) {
            throw new BusinessException(403, "无权查看该作业题目");
        }
        return list(new QueryWrapper<Question>()
                .eq("assignment_id", assignmentId)
                .orderByAsc("id"));
    }

    private void validateQuestion(String questionType, QuestionCreateRequest request, Assignment assignment) {
        if (!SUPPORTED_QUESTION_TYPES.contains(questionType)) {
            throw new BusinessException(400, "当前只支持单选题和判断题，多选题作为后续增强");
        }
        if (!questionType.equals(assignment.getAssignmentType())) {
            throw new BusinessException(400, "题目类型必须与作业类型一致");
        }
        String answer = normalizeAnswer(questionType, request.getStandardAnswer());
        if (answer.isEmpty()) {
            throw new BusinessException(400, "标准答案不能为空");
        }
        if ("SINGLE_CHOICE".equals(questionType) && (request.getOptions() == null || request.getOptions().trim().isEmpty())) {
            throw new BusinessException(400, "单选题必须设置选项");
        }
        if ("TRUE_FALSE".equals(questionType) && !("TRUE".equals(answer) || "FALSE".equals(answer))) {
            throw new BusinessException(400, "判断题标准答案必须是 TRUE 或 FALSE");
        }
    }

    private String normalizeAnswer(String questionType, String answer) {
        String normalized = answer == null ? "" : answer.trim().toUpperCase();
        if ("TRUE_FALSE".equals(questionType)) {
            if ("对".equals(answer) || "正确".equals(answer)) {
                return "TRUE";
            }
            if ("错".equals(answer) || "错误".equals(answer)) {
                return "FALSE";
            }
        }
        return normalized;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private void ensureLogin(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(401, "请先登录");
        }
    }
}
