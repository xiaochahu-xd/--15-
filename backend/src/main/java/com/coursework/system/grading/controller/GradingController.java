package com.coursework.system.grading.controller;

import com.coursework.system.common.response.ApiResponse;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.common.utils.RequestUtils;
import com.coursework.system.grading.dto.GradeRequest;
import com.coursework.system.grading.dto.GradingDetail;
import com.coursework.system.grading.dto.GradingItem;
import com.coursework.system.grading.dto.GradingProgress;
import com.coursework.system.grading.dto.ReturnSubmissionRequest;
import com.coursework.system.grading.service.GradingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
public class GradingController {
    private final GradingService gradingService;

    public GradingController(GradingService gradingService) {
        this.gradingService = gradingService;
    }

    @GetMapping("/api/assignments/{assignmentId}/grading-list")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','ASSISTANT')")
    public ApiResponse<List<GradingItem>> gradingList(@PathVariable Long assignmentId,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(gradingService.listGradingList(assignmentId, principal));
    }

    @GetMapping("/api/submissions/{submissionId}/detail")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','ASSISTANT')")
    public ApiResponse<GradingDetail> gradingDetail(@PathVariable Long submissionId,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(gradingService.getSubmissionDetail(submissionId, principal));
    }

    @PostMapping("/api/submissions/{submissionId}/grade")
    @PreAuthorize("hasAnyRole('TEACHER','ASSISTANT')")
    public ApiResponse<GradingDetail> saveGrade(@PathVariable Long submissionId,
                                                @Valid @RequestBody GradeRequest request,
                                                @AuthenticationPrincipal UserPrincipal principal,
                                                HttpServletRequest servletRequest) {
        return ApiResponse.success("批改结果已保存",
                gradingService.saveGrade(submissionId, request, principal, RequestUtils.getClientIp(servletRequest)));
    }

    @PutMapping("/api/submissions/{submissionId}/return")
    @PreAuthorize("hasAnyRole('TEACHER','ASSISTANT')")
    public ApiResponse<GradingDetail> returnSubmission(@PathVariable Long submissionId,
                                                       @Valid @RequestBody ReturnSubmissionRequest request,
                                                       @AuthenticationPrincipal UserPrincipal principal,
                                                       HttpServletRequest servletRequest) {
        return ApiResponse.success("作业已退回修改",
                gradingService.returnSubmission(submissionId, request, principal, RequestUtils.getClientIp(servletRequest)));
    }

    @GetMapping("/api/submissions/{submissionId}/grade")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<GradingDetail> gradeDetail(@PathVariable Long submissionId,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(gradingService.getGrade(submissionId, principal));
    }

    @GetMapping("/api/courses/{courseId}/grading-progress")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','ASSISTANT')")
    public ApiResponse<GradingProgress> gradingProgress(@PathVariable Long courseId,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(gradingService.getCourseProgress(courseId, principal));
    }
}
