package com.coursework.system.statistics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GradeExportRow {
    private String studentNo;
    private String studentName;
    private String courseName;
    private String assignmentName;
    private LocalDateTime submitTime;
    private Boolean late;
    private Boolean suspectedDuplicate;
    private BigDecimal autoScore;
    private BigDecimal manualScore;
    private BigDecimal finalScore;
    private String comment;
    private String graderName;

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getAssignmentName() { return assignmentName; }
    public void setAssignmentName(String assignmentName) { this.assignmentName = assignmentName; }
    public LocalDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }
    public Boolean getLate() { return late; }
    public void setLate(Boolean late) { this.late = late; }
    public Boolean getSuspectedDuplicate() { return suspectedDuplicate; }
    public void setSuspectedDuplicate(Boolean suspectedDuplicate) { this.suspectedDuplicate = suspectedDuplicate; }
    public BigDecimal getAutoScore() { return autoScore; }
    public void setAutoScore(BigDecimal autoScore) { this.autoScore = autoScore; }
    public BigDecimal getManualScore() { return manualScore; }
    public void setManualScore(BigDecimal manualScore) { this.manualScore = manualScore; }
    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getGraderName() { return graderName; }
    public void setGraderName(String graderName) { this.graderName = graderName; }
}
