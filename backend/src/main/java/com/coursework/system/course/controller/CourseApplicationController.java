package com.coursework.system.course.controller;

import com.coursework.system.common.response.ApiResponse;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.common.utils.RequestUtils;
import com.coursework.system.course.dto.CourseApplicationCreateRequest;
import com.coursework.system.course.dto.CourseApplicationRejectRequest;
import com.coursework.system.course.dto.CourseApplicationSummary;
import com.coursework.system.course.service.CourseApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/course-applications")
public class CourseApplicationController {
    private final CourseApplicationService courseApplicationService;

    public CourseApplicationController(CourseApplicationService courseApplicationService) {
        this.courseApplicationService = courseApplicationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<CourseApplicationSummary> submit(@Valid @RequestBody CourseApplicationCreateRequest request,
                                                        @AuthenticationPrincipal UserPrincipal principal,
                                                        HttpServletRequest servletRequest) {
        return ApiResponse.success("课程申请已提交",
                courseApplicationService.submitApplication(request, principal, RequestUtils.getClientIp(servletRequest)));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CourseApplicationSummary>> pending() {
        return ApiResponse.success(courseApplicationService.listPending());
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CourseApplicationSummary> approve(@PathVariable Long id,
                                                         @AuthenticationPrincipal UserPrincipal principal,
                                                         HttpServletRequest servletRequest) {
        return ApiResponse.success("审批通过",
                courseApplicationService.approve(id, principal, RequestUtils.getClientIp(servletRequest)));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CourseApplicationSummary> reject(@PathVariable Long id,
                                                        @Valid @RequestBody CourseApplicationRejectRequest request,
                                                        @AuthenticationPrincipal UserPrincipal principal,
                                                        HttpServletRequest servletRequest) {
        return ApiResponse.success("已驳回",
                courseApplicationService.reject(id, request, principal, RequestUtils.getClientIp(servletRequest)));
    }
}
