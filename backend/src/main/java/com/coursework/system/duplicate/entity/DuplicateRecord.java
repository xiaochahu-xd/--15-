package com.coursework.system.duplicate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("duplicate_records")
public class DuplicateRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("assignment_id")
    private Long assignmentId;
    @TableField("file_hash")
    private String fileHash;
    @TableField("submission_ids")
    private String submissionIds;
    @TableField("source_submission_id")
    private Long sourceSubmissionId;
    @TableField("matched_submission_id")
    private Long matchedSubmissionId;
    @TableField("source_student_id")
    private Long sourceStudentId;
    @TableField("matched_student_id")
    private Long matchedStudentId;
    @TableField("detection_type")
    private String detectionType;
    @TableField("similarity_score")
    private java.math.BigDecimal similarityScore;
    private java.math.BigDecimal threshold;
    @TableField("detected_at")
    private LocalDateTime detectedAt;
    private String status;
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getSubmissionIds() {
        return submissionIds;
    }

    public void setSubmissionIds(String submissionIds) {
        this.submissionIds = submissionIds;
    }

    public Long getSourceSubmissionId() {
        return sourceSubmissionId;
    }

    public void setSourceSubmissionId(Long sourceSubmissionId) {
        this.sourceSubmissionId = sourceSubmissionId;
    }

    public Long getMatchedSubmissionId() {
        return matchedSubmissionId;
    }

    public void setMatchedSubmissionId(Long matchedSubmissionId) {
        this.matchedSubmissionId = matchedSubmissionId;
    }

    public Long getSourceStudentId() {
        return sourceStudentId;
    }

    public void setSourceStudentId(Long sourceStudentId) {
        this.sourceStudentId = sourceStudentId;
    }

    public Long getMatchedStudentId() {
        return matchedStudentId;
    }

    public void setMatchedStudentId(Long matchedStudentId) {
        this.matchedStudentId = matchedStudentId;
    }

    public String getDetectionType() {
        return detectionType;
    }

    public void setDetectionType(String detectionType) {
        this.detectionType = detectionType;
    }

    public java.math.BigDecimal getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(java.math.BigDecimal similarityScore) {
        this.similarityScore = similarityScore;
    }

    public java.math.BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(java.math.BigDecimal threshold) {
        this.threshold = threshold;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
