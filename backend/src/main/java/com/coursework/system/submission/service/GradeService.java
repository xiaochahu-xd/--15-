package com.coursework.system.submission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursework.system.submission.entity.Grade;

import java.math.BigDecimal;

public interface GradeService extends IService<Grade> {
    void createPendingGrade(Long submissionId);

    Grade getBySubmissionId(Long submissionId);

    void applyAutoScore(Long submissionId, BigDecimal score);
}
