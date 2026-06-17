package com.coursework.system.dashboard.controller;

import com.coursework.system.common.response.ApiResponse;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.dashboard.dto.DashboardData;
import com.coursework.system.dashboard.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<DashboardData> dashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(dashboardService.getDashboard(principal));
    }
}
