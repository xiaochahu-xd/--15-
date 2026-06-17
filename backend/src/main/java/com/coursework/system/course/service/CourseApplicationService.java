package com.coursework.system.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.course.dto.CourseApplicationCreateRequest;
import com.coursework.system.course.dto.CourseApplicationRejectRequest;
import com.coursework.system.course.dto.CourseApplicationSummary;
import com.coursework.system.course.entity.CourseApplication;

import java.util.List;

public interface CourseApplicationService extends IService<CourseApplication> {
    CourseApplicationSummary submitApplication(CourseApplicationCreateRequest request, UserPrincipal principal, String ip);

    List<CourseApplicationSummary> listPending();

    CourseApplicationSummary approve(Long applicationId, UserPrincipal principal, String ip);

    CourseApplicationSummary reject(Long applicationId, CourseApplicationRejectRequest request, UserPrincipal principal, String ip);
}
