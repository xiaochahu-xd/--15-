package com.coursework.system.assignment.controller;

import com.coursework.system.assignment.dto.AssignmentCreateRequest;
import com.coursework.system.assignment.dto.AssignmentSummary;
import com.coursework.system.assignment.dto.AssignmentUpdateRequest;
import com.coursework.system.assignment.service.AssignmentService;
import com.coursework.system.common.response.ApiResponse;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.common.utils.RequestUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
public class AssignmentController {
    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/api/courses/{courseId}/assignments")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ApiResponse<AssignmentSummary> create(@PathVariable Long courseId,
                                                 @Valid @RequestBody AssignmentCreateRequest request,
                                                 @AuthenticationPrincipal UserPrincipal principal,
                                                 HttpServletRequest servletRequest) {
        return ApiResponse.success("作业已发布",
                assignmentService.createAssignment(courseId, request, principal, RequestUtils.getClientIp(servletRequest)));
    }

    @GetMapping("/api/courses/{courseId}/assignments")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<AssignmentSummary>> listByCourse(@PathVariable Long courseId,
                                                             @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(assignmentService.listByCourse(courseId, principal));
    }

    @GetMapping("/api/assignments/{assignmentId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AssignmentSummary> detail(@PathVariable Long assignmentId,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(assignmentService.getDetail(assignmentId, principal));
    }

    @PutMapping("/api/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ApiResponse<AssignmentSummary> update(@PathVariable Long assignmentId,
                                                 @Valid @RequestBody AssignmentUpdateRequest request,
                                                 @AuthenticationPrincipal UserPrincipal principal,
                                                 HttpServletRequest servletRequest) {
        return ApiResponse.success("作业已修改",
                assignmentService.updateAssignment(assignmentId, request, principal, RequestUtils.getClientIp(servletRequest)));
    }

    @DeleteMapping("/api/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ApiResponse<Void> delete(@PathVariable Long assignmentId,
                                    @AuthenticationPrincipal UserPrincipal principal,
                                    HttpServletRequest servletRequest) {
        assignmentService.deleteAssignment(assignmentId, principal, RequestUtils.getClientIp(servletRequest));
        return ApiResponse.success("作业已删除", null);
    }
}
