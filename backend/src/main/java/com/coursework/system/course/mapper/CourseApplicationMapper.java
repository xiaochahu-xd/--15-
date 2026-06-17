package com.coursework.system.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursework.system.course.dto.CourseApplicationSummary;
import com.coursework.system.course.entity.CourseApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseApplicationMapper extends BaseMapper<CourseApplication> {

    @Select("SELECT ca.*, t.real_name AS teacher_name, r.real_name AS reviewed_by_name " +
            "FROM course_applications ca " +
            "LEFT JOIN users t ON ca.teacher_id = t.id " +
            "LEFT JOIN users r ON ca.reviewed_by = r.id " +
            "WHERE ca.status = 'PENDING' " +
            "ORDER BY ca.created_at DESC")
    List<CourseApplicationSummary> selectPendingSummaries();
}
