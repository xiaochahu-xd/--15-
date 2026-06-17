package com.coursework.system.dashboard.service;

import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.dashboard.dto.DashboardData;

public interface DashboardService {
    DashboardData getDashboard(UserPrincipal principal);
}
