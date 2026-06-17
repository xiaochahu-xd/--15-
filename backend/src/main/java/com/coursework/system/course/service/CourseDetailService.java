package com.coursework.system.course.service;

import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.course.dto.CourseDetailSummary;

public interface CourseDetailService {
    CourseDetailSummary getDetail(Long courseId, UserPrincipal principal);
}
