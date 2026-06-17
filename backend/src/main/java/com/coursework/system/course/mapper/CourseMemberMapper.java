package com.coursework.system.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursework.system.course.dto.CourseMemberSummary;
import com.coursework.system.course.entity.CourseMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMemberMapper extends BaseMapper<CourseMember> {

    @Select("SELECT cm.*, u.username, u.real_name " +
            "FROM course_members cm " +
            "INNER JOIN users u ON cm.user_id = u.id " +
            "WHERE cm.course_id = #{courseId} AND cm.status = 1 " +
            "ORDER BY FIELD(cm.member_role, 'OWNER', 'TEACHER', 'ASSISTANT', 'STUDENT'), cm.joined_at ASC")
    List<CourseMemberSummary> selectActiveMembers(Long courseId);
}
