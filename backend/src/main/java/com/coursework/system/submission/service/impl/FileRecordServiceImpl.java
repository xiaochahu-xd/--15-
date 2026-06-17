package com.coursework.system.submission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursework.system.submission.entity.FileRecord;
import com.coursework.system.submission.mapper.FileRecordMapper;
import com.coursework.system.submission.service.FileRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileRecordServiceImpl extends ServiceImpl<FileRecordMapper, FileRecord> implements FileRecordService {

    @Override
    public List<FileRecord> listBySubmissionId(Long submissionId) {
        return list(new QueryWrapper<FileRecord>()
                .eq("submission_id", submissionId)
                .orderByAsc("uploaded_at"));
    }

    @Override
    public List<FileRecord> listByAssignmentId(Long assignmentId) {
        return baseMapper.selectByAssignmentId(assignmentId);
    }
}
