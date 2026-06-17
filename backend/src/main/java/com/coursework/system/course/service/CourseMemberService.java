package com.coursework.system.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursework.system.course.dto.CourseMemberSummary;
import com.coursework.system.course.entity.CourseMember;

import java.util.List;

public interface CourseMemberService extends IService<CourseMember> {
    CourseMember getByCourseIdAndUserId(Long courseId, Long userId);

    boolean isActiveMember(Long courseId, Long userId);

    List<CourseMemberSummary> listActiveMembers(Long courseId);
}
