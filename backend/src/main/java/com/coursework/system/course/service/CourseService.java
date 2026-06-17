package com.coursework.system.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.course.dto.CourseMemberAddRequest;
import com.coursework.system.course.dto.CourseMemberSummary;
import com.coursework.system.course.dto.CourseSummary;
import com.coursework.system.course.entity.Course;

import java.util.List;

public interface CourseService extends IService<Course> {
    Course getByCourseCode(String courseCode);

    List<CourseSummary> listMyCourses(UserPrincipal principal);

    List<CourseSummary> listAllCourses();

    List<CourseMemberSummary> listMembers(Long courseId, UserPrincipal principal);

    CourseMemberSummary addMember(Long courseId, CourseMemberAddRequest request, UserPrincipal principal, String ip);

    void removeMember(Long courseId, Long userId, UserPrincipal principal, String ip);

    boolean canManageCourse(Long courseId, UserPrincipal principal);

    boolean canViewCourse(Long courseId, UserPrincipal principal);
}
