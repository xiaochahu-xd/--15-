package com.coursework.system.duplicate.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class DuplicateRecordSummary {
    private Long id;
    private Long assignmentId;
    private String assignmentTitle;
    private String fileHash;
    private String submissionIds;
    private List<Long> submissionIdList = new ArrayList<Long>();
    private Long sourceSubmissionId;
    private Long matchedSubmissionId;
    private Long sourceStudentId;
    private Long matchedStudentId;
    private String sourceStudentName;
    private String matchedStudentName;
    private String detectionType;
    private BigDecimal similarityScore;
    private BigDecimal threshold;
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

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
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

    public List<Long> getSubmissionIdList() {
        return submissionIdList;
    }

    public void setSubmissionIdList(List<Long> submissionIdList) {
        this.submissionIdList = submissionIdList;
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

    public String getSourceStudentName() {
        return sourceStudentName;
    }

    public void setSourceStudentName(String sourceStudentName) {
        this.sourceStudentName = sourceStudentName;
    }

    public String getMatchedStudentName() {
        return matchedStudentName;
    }

    public void setMatchedStudentName(String matchedStudentName) {
        this.matchedStudentName = matchedStudentName;
    }

    public String getDetectionType() {
        return detectionType;
    }

    public void setDetectionType(String detectionType) {
        this.detectionType = detectionType;
    }

    public BigDecimal getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(BigDecimal similarityScore) {
        this.similarityScore = similarityScore;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
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
