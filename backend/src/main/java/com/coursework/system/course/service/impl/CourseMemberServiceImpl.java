package com.coursework.system.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursework.system.course.dto.CourseMemberSummary;
import com.coursework.system.course.entity.CourseMember;
import com.coursework.system.course.mapper.CourseMemberMapper;
import com.coursework.system.course.service.CourseMemberService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseMemberServiceImpl extends ServiceImpl<CourseMemberMapper, CourseMember> implements CourseMemberService {

    @Override
    public CourseMember getByCourseIdAndUserId(Long courseId, Long userId) {
        return getOne(new QueryWrapper<CourseMember>()
                .eq("course_id", courseId)
                .eq("user_id", userId), false);
    }

    @Override
    public boolean isActiveMember(Long courseId, Long userId) {
        return count(new QueryWrapper<CourseMember>()
                .eq("course_id", courseId)
                .eq("user_id", userId)
                .eq("status", 1)) > 0;
    }

    @Override
    public List<CourseMemberSummary> listActiveMembers(Long courseId) {
        return baseMapper.selectActiveMembers(courseId);
    }
}
