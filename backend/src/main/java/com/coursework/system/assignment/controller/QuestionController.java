package com.coursework.system.assignment.controller;

import com.coursework.system.assignment.dto.QuestionCreateRequest;
import com.coursework.system.assignment.entity.Question;
import com.coursework.system.assignment.service.QuestionService;
import com.coursework.system.common.response.ApiResponse;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.common.utils.RequestUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/assignments/{assignmentId}/questions")
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ApiResponse<Question> addQuestion(@PathVariable Long assignmentId,
                                             @Valid @RequestBody QuestionCreateRequest request,
                                             @AuthenticationPrincipal UserPrincipal principal,
                                             HttpServletRequest servletRequest) {
        return ApiResponse.success("题目已添加",
                questionService.addQuestion(assignmentId, request, principal, RequestUtils.getClientIp(servletRequest)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Question>> listQuestions(@PathVariable Long assignmentId,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(questionService.listQuestions(assignmentId, principal));
    }
}
