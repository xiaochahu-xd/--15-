package com.coursework.system.submission.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SubmissionSummary {
    private Long id;
    private Long assignmentId;
    private String assignmentTitle;
    private String assignmentType;
    private Long courseId;
    private String courseName;
    private Long studentId;
    private String studentUsername;
    private String studentName;
    private String content;
    private LocalDateTime submitTime;
    private Boolean late;
    private String status;
    private BigDecimal autoScore;
    private BigDecimal finalScore;
    private Boolean suspectedDuplicate;
    private Boolean hasSimilarityAlert;
    private BigDecimal similarityScore;
    private Integer fileCount;

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

    public String getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public Boolean getLate() {
        return late;
    }

    public void setLate(Boolean late) {
        this.late = late;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAutoScore() {
        return autoScore;
    }

    public void setAutoScore(BigDecimal autoScore) {
        this.autoScore = autoScore;
    }

    public BigDecimal getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(BigDecimal finalScore) {
        this.finalScore = finalScore;
    }

    public Boolean getSuspectedDuplicate() {
        return suspectedDuplicate;
    }

    public void setSuspectedDuplicate(Boolean suspectedDuplicate) {
        this.suspectedDuplicate = suspectedDuplicate;
    }

    public Boolean getHasSimilarityAlert() {
        return hasSimilarityAlert;
    }

    public void setHasSimilarityAlert(Boolean hasSimilarityAlert) {
        this.hasSimilarityAlert = hasSimilarityAlert;
    }

    public BigDecimal getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(BigDecimal similarityScore) {
        this.similarityScore = similarityScore;
    }

    public Integer getFileCount() {
        return fileCount;
    }

    public void setFileCount(Integer fileCount) {
        this.fileCount = fileCount;
    }
}
