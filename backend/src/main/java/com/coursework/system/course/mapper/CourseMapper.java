package com.coursework.system.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursework.system.course.dto.CourseSummary;
import com.coursework.system.course.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    @Select("SELECT c.*, u.real_name AS owner_name, " +
            "CASE WHEN c.owner_id = #{userId} THEN 'OWNER' ELSE cm.member_role END AS member_role " +
            "FROM courses c " +
            "LEFT JOIN users u ON c.owner_id = u.id " +
            "LEFT JOIN course_members cm ON c.id = cm.course_id AND cm.user_id = #{userId} AND cm.status = 1 " +
            "WHERE c.status = 'ACTIVE' AND (c.owner_id = #{userId} OR cm.user_id IS NOT NULL) " +
            "ORDER BY c.created_at DESC")
    List<CourseSummary> selectSummariesForUser(Long userId);

    @Select("SELECT c.*, u.real_name AS owner_name, 'ADMIN' AS member_role " +
            "FROM courses c LEFT JOIN users u ON c.owner_id = u.id " +
            "ORDER BY c.created_at DESC")
    List<CourseSummary> selectAllSummaries();
}
