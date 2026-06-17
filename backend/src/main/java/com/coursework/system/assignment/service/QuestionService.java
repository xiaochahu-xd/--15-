package com.coursework.system.assignment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursework.system.assignment.dto.QuestionCreateRequest;
import com.coursework.system.assignment.entity.Question;
import com.coursework.system.common.security.UserPrincipal;

import java.util.List;

public interface QuestionService extends IService<Question> {
    Question addQuestion(Long assignmentId, QuestionCreateRequest request, UserPrincipal principal, String ip);

    List<Question> listQuestions(Long assignmentId, UserPrincipal principal);
}
