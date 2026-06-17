package com.coursework.system.statistics.service;

import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.statistics.dto.AssignmentStatistics;
import com.coursework.system.statistics.dto.CourseStatistics;
import com.coursework.system.statistics.dto.GradeExportRow;

import java.util.List;

public interface StatisticsService {
    CourseStatistics getCourseStatistics(Long courseId, UserPrincipal principal);

    AssignmentStatistics getAssignmentStatistics(Long assignmentId, UserPrincipal principal);

    List<GradeExportRow> listCourseGrades(Long courseId, UserPrincipal principal);

    byte[] exportCourseGrades(Long courseId, UserPrincipal principal, String ip);
}
