package com.coursework.system.statistics.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AssignmentStatistics {
    private Long assignmentId;
    private String assignmentTitle;
    private Long courseId;
    private String courseName;
    private Integer studentCount;
    private Integer submissionCount;
    private BigDecimal submissionRate;
    private Integer lateCount;
    private BigDecimal lateRate;
    private BigDecimal averageScore;
    private BigDecimal highestScore;
    private BigDecimal lowestScore;
    private Integer duplicateCount;
    private List<ScoreBucket> scoreDistribution = new ArrayList<ScoreBucket>();

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    public String getAssignmentTitle() { return assignmentTitle; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }
    public Integer getSubmissionCount() { return submissionCount; }
    public void setSubmissionCount(Integer submissionCount) { this.submissionCount = submissionCount; }
    public BigDecimal getSubmissionRate() { return submissionRate; }
    public void setSubmissionRate(BigDecimal submissionRate) { this.submissionRate = submissionRate; }
    public Integer getLateCount() { return lateCount; }
    public void setLateCount(Integer lateCount) { this.lateCount = lateCount; }
    public BigDecimal getLateRate() { return lateRate; }
    public void setLateRate(BigDecimal lateRate) { this.lateRate = lateRate; }
    public BigDecimal getAverageScore() { return averageScore; }
    public void setAverageScore(BigDecimal averageScore) { this.averageScore = averageScore; }
    public BigDecimal getHighestScore() { return highestScore; }
    public void setHighestScore(BigDecimal highestScore) { this.highestScore = highestScore; }
    public BigDecimal getLowestScore() { return lowestScore; }
    public void setLowestScore(BigDecimal lowestScore) { this.lowestScore = lowestScore; }
    public Integer getDuplicateCount() { return duplicateCount; }
    public void setDuplicateCount(Integer duplicateCount) { this.duplicateCount = duplicateCount; }
    public List<ScoreBucket> getScoreDistribution() { return scoreDistribution; }
    public void setScoreDistribution(List<ScoreBucket> scoreDistribution) { this.scoreDistribution = scoreDistribution; }
}
