package com.coursework.system.statistics.controller;

import com.coursework.system.common.response.ApiResponse;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.common.utils.RequestUtils;
import com.coursework.system.statistics.dto.AssignmentStatistics;
import com.coursework.system.statistics.dto.CourseStatistics;
import com.coursework.system.statistics.dto.GradeExportRow;
import com.coursework.system.statistics.service.StatisticsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/api/courses/{courseId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','ASSISTANT')")
    public ApiResponse<CourseStatistics> courseStatistics(@PathVariable Long courseId,
                                                          @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(statisticsService.getCourseStatistics(courseId, principal));
    }

    @GetMapping("/api/assignments/{assignmentId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','ASSISTANT')")
    public ApiResponse<AssignmentStatistics> assignmentStatistics(@PathVariable Long assignmentId,
                                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(statisticsService.getAssignmentStatistics(assignmentId, principal));
    }

    @GetMapping("/api/courses/{courseId}/grades")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','ASSISTANT')")
    public ApiResponse<List<GradeExportRow>> courseGrades(@PathVariable Long courseId,
                                                          @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(statisticsService.listCourseGrades(courseId, principal));
    }

    @GetMapping("/api/courses/{courseId}/grades/export")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<byte[]> exportCourseGrades(@PathVariable Long courseId,
                                                     @AuthenticationPrincipal UserPrincipal principal,
                                                     HttpServletRequest servletRequest) throws Exception {
        byte[] bytes = statisticsService.exportCourseGrades(courseId, principal, RequestUtils.getClientIp(servletRequest));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = URLEncoder.encode("课程成绩-" + courseId + "-" + timestamp + ".xlsx",
                StandardCharsets.UTF_8.name()).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}
