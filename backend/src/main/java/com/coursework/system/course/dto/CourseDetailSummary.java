package com.coursework.system.course.dto;

import java.math.BigDecimal;

public class CourseDetailSummary {
    private CourseSummary course;
    private String myRole;
    private Long memberCount;
    private Long assignmentCount;
    private Long submissionCount;
    private Long pendingSubmissionCount;
    private Long pendingGradingCount;
    private Long duplicateCount;
    private BigDecimal averageScore;
    private BigDecimal submissionRate;

    public CourseSummary getCourse() { return course; }
    public void setCourse(CourseSummary course) { this.course = course; }
    public String getMyRole() { return myRole; }
    public void setMyRole(String myRole) { this.myRole = myRole; }
    public Long getMemberCount() { return memberCount; }
    public void setMemberCount(Long memberCount) { this.memberCount = memberCount; }
    public Long getAssignmentCount() { return assignmentCount; }
    public void setAssignmentCount(Long assignmentCount) { this.assignmentCount = assignmentCount; }
    public Long getSubmissionCount() { return submissionCount; }
    public void setSubmissionCount(Long submissionCount) { this.submissionCount = submissionCount; }
    public Long getPendingSubmissionCount() { return pendingSubmissionCount; }
    public void setPendingSubmissionCount(Long pendingSubmissionCount) { this.pendingSubmissionCount = pendingSubmissionCount; }
    public Long getPendingGradingCount() { return pendingGradingCount; }
    public void setPendingGradingCount(Long pendingGradingCount) { this.pendingGradingCount = pendingGradingCount; }
    public Long getDuplicateCount() { return duplicateCount; }
    public void setDuplicateCount(Long duplicateCount) { this.duplicateCount = duplicateCount; }
    public BigDecimal getAverageScore() { return averageScore; }
    public void setAverageScore(BigDecimal averageScore) { this.averageScore = averageScore; }
    public BigDecimal getSubmissionRate() { return submissionRate; }
    public void setSubmissionRate(BigDecimal submissionRate) { this.submissionRate = submissionRate; }
}
