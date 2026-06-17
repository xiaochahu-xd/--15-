package com.coursework.system.course.controller;

import com.coursework.system.common.response.ApiResponse;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.common.utils.RequestUtils;
import com.coursework.system.course.dto.CourseDetailSummary;
import com.coursework.system.course.dto.CourseMemberAddRequest;
import com.coursework.system.course.dto.CourseMemberSummary;
import com.coursework.system.course.dto.CourseSummary;
import com.coursework.system.course.service.CourseDetailService;
import com.coursework.system.course.service.CourseService;
import com.coursework.system.notification.entity.Notification;
import com.coursework.system.notification.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseService courseService;
    private final CourseDetailService courseDetailService;
    private final NotificationService notificationService;

    public CourseController(CourseService courseService,
                            CourseDetailService courseDetailService,
                            NotificationService notificationService) {
        this.courseService = courseService;
        this.courseDetailService = courseDetailService;
        this.notificationService = notificationService;
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<CourseSummary>> myCourses(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(courseService.listMyCourses(principal));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CourseSummary>> allCourses() {
        return ApiResponse.success(courseService.listAllCourses());
    }

    @PostMapping("/{courseId}/members")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ApiResponse<CourseMemberSummary> addMember(@PathVariable Long courseId,
                                                      @Valid @RequestBody CourseMemberAddRequest request,
                                                      @AuthenticationPrincipal UserPrincipal principal,
                                                      HttpServletRequest servletRequest) {
        return ApiResponse.success("成员已添加",
                courseService.addMember(courseId, request, principal, RequestUtils.getClientIp(servletRequest)));
    }

    @DeleteMapping("/{courseId}/members/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ApiResponse<Void> removeMember(@PathVariable Long courseId,
                                          @PathVariable Long userId,
                                          @AuthenticationPrincipal UserPrincipal principal,
                                          HttpServletRequest servletRequest) {
        courseService.removeMember(courseId, userId, principal, RequestUtils.getClientIp(servletRequest));
        return ApiResponse.success("成员已移除", null);
    }

    @GetMapping("/{courseId}/members")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<CourseMemberSummary>> members(@PathVariable Long courseId,
                                                          @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(courseService.listMembers(courseId, principal));
    }

    @GetMapping("/{courseId}/detail")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CourseDetailSummary> detail(@PathVariable Long courseId,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(courseDetailService.getDetail(courseId, principal));
    }

    @GetMapping("/{courseId}/notifications")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Notification>> notifications(@PathVariable Long courseId,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(notificationService.listForCourse(courseId, principal));
    }
}
