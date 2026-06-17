package com.coursework.system.assignment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursework.system.assignment.dto.AssignmentCreateRequest;
import com.coursework.system.assignment.dto.AssignmentSummary;
import com.coursework.system.assignment.dto.AssignmentUpdateRequest;
import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.common.security.UserPrincipal;

import java.util.List;

public interface AssignmentService extends IService<Assignment> {
    AssignmentSummary createAssignment(Long courseId, AssignmentCreateRequest request, UserPrincipal principal, String ip);

    List<AssignmentSummary> listByCourse(Long courseId, UserPrincipal principal);

    AssignmentSummary getDetail(Long assignmentId, UserPrincipal principal);

    AssignmentSummary updateAssignment(Long assignmentId, AssignmentUpdateRequest request, UserPrincipal principal, String ip);

    void deleteAssignment(Long assignmentId, UserPrincipal principal, String ip);

    Assignment getActiveAssignment(Long assignmentId);

    boolean canEditAssignment(Assignment assignment, UserPrincipal principal);

    boolean canViewAssignment(Assignment assignment, UserPrincipal principal);
}
