package com.coursework.system.submission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursework.system.submission.entity.FileRecord;

import java.util.List;

public interface FileRecordService extends IService<FileRecord> {
    List<FileRecord> listBySubmissionId(Long submissionId);

    List<FileRecord> listByAssignmentId(Long assignmentId);
}
