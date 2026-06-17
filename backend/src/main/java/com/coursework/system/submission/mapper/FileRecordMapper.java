package com.coursework.system.submission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursework.system.submission.entity.FileRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FileRecordMapper extends BaseMapper<FileRecord> {

    @Select("SELECT fr.* FROM file_records fr " +
            "INNER JOIN submissions s ON fr.submission_id = s.id " +
            "WHERE s.assignment_id = #{assignmentId} " +
            "ORDER BY fr.uploaded_at ASC")
    List<FileRecord> selectByAssignmentId(Long assignmentId);
}
