package com.coursework.system.submission.service;

import com.coursework.system.submission.entity.FileRecord;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    FileRecord store(Long submissionId, Long assignmentId, MultipartFile file);
}
