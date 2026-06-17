package com.coursework.system.grading.dto;

import java.math.BigDecimal;

public class GradingProgress {
    private Long courseId;
    private String courseName;
    private Integer assignmentCount;
    private Integer submissionCount;
    private Integer pendingCount;
    private Integer autoGradedCount;
    private Integer gradedCount;
    private Integer returnedCount;
    private BigDecimal gradingRate;

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

    public Integer getAssignmentCount() {
        return assignmentCount;
    }

    public void setAssignmentCount(Integer assignmentCount) {
        this.assignmentCount = assignmentCount;
    }

    public Integer getSubmissionCount() {
        return submissionCount;
    }

    public void setSubmissionCount(Integer submissionCount) {
        this.submissionCount = submissionCount;
    }

    public Integer getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Integer pendingCount) {
        this.pendingCount = pendingCount;
    }

    public Integer getAutoGradedCount() {
        return autoGradedCount;
    }

    public void setAutoGradedCount(Integer autoGradedCount) {
        this.autoGradedCount = autoGradedCount;
    }

    public Integer getGradedCount() {
        return gradedCount;
    }

    public void setGradedCount(Integer gradedCount) {
        this.gradedCount = gradedCount;
    }

    public Integer getReturnedCount() {
        return returnedCount;
    }

    public void setReturnedCount(Integer returnedCount) {
        this.returnedCount = returnedCount;
    }

    public BigDecimal getGradingRate() {
        return gradingRate;
    }

    public void setGradingRate(BigDecimal gradingRate) {
        this.gradingRate = gradingRate;
    }
}
