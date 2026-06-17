package com.coursework.system.submission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursework.system.submission.entity.Grade;
import com.coursework.system.submission.mapper.GradeMapper;
import com.coursework.system.submission.service.GradeService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class GradeServiceImpl extends ServiceImpl<GradeMapper, Grade> implements GradeService {

    @Override
    public void createPendingGrade(Long submissionId) {
        Grade grade = new Grade();
        grade.setSubmissionId(submissionId);
        grade.setGradeStatus("PENDING");
        save(grade);
    }

    @Override
    public Grade getBySubmissionId(Long submissionId) {
        return getOne(new QueryWrapper<Grade>().eq("submission_id", submissionId), false);
    }

    @Override
    public void applyAutoScore(Long submissionId, BigDecimal score) {
        Grade grade = getBySubmissionId(submissionId);
        if (grade == null) {
            grade = new Grade();
            grade.setSubmissionId(submissionId);
        }
        grade.setScore(score);
        grade.setGradeStatus("AUTO_GRADED");
        grade.setGradedAt(LocalDateTime.now());
        saveOrUpdate(grade);
    }
}
