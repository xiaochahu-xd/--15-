package com.coursework.system.dashboard.dto;

import com.coursework.system.course.dto.CourseApplicationSummary;
import com.coursework.system.course.dto.CourseSummary;
import com.coursework.system.log.entity.OperationLog;
import com.coursework.system.notification.entity.Notification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DashboardData {
    private String primaryRole;
    private LocalDateTime generatedAt;
    private Long pendingCourseApplications;
    private Long approvedCourseApplications;
    private Long userCount;
    private Long teacherCount;
    private Long studentCount;
    private Long assistantCount;
    private Long myCourseCount;
    private Long assignmentCount;
    private Long pendingAssignmentCount;
    private Long submittedAssignmentCount;
    private Long pendingGradingCount;
    private Long gradedCount;
    private Long duplicateCount;
    private Long unreadNotificationCount;
    private List<CourseApplicationSummary> recentApplications = new ArrayList<CourseApplicationSummary>();
    private List<CourseSummary> recentCourses = new ArrayList<CourseSummary>();
    private List<OperationLog> recentLogs = new ArrayList<OperationLog>();
    private List<Notification> recentNotifications = new ArrayList<Notification>();

    public String getPrimaryRole() { return primaryRole; }
    public void setPrimaryRole(String primaryRole) { this.primaryRole = primaryRole; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public Long getPendingCourseApplications() { return pendingCourseApplications; }
    public void setPendingCourseApplications(Long pendingCourseApplications) { this.pendingCourseApplications = pendingCourseApplications; }
    public Long getApprovedCourseApplications() { return approvedCourseApplications; }
    public void setApprovedCourseApplications(Long approvedCourseApplications) { this.approvedCourseApplications = approvedCourseApplications; }
    public Long getUserCount() { return userCount; }
    public void setUserCount(Long userCount) { this.userCount = userCount; }
    public Long getTeacherCount() { return teacherCount; }
    public void setTeacherCount(Long teacherCount) { this.teacherCount = teacherCount; }
    public Long getStudentCount() { return studentCount; }
    public void setStudentCount(Long studentCount) { this.studentCount = studentCount; }
    public Long getAssistantCount() { return assistantCount; }
    public void setAssistantCount(Long assistantCount) { this.assistantCount = assistantCount; }
    public Long getMyCourseCount() { return myCourseCount; }
    public void setMyCourseCount(Long myCourseCount) { this.myCourseCount = myCourseCount; }
    public Long getAssignmentCount() { return assignmentCount; }
    public void setAssignmentCount(Long assignmentCount) { this.assignmentCount = assignmentCount; }
    public Long getPendingAssignmentCount() { return pendingAssignmentCount; }
    public void setPendingAssignmentCount(Long pendingAssignmentCount) { this.pendingAssignmentCount = pendingAssignmentCount; }
    public Long getSubmittedAssignmentCount() { return submittedAssignmentCount; }
    public void setSubmittedAssignmentCount(Long submittedAssignmentCount) { this.submittedAssignmentCount = submittedAssignmentCount; }
    public Long getPendingGradingCount() { return pendingGradingCount; }
    public void setPendingGradingCount(Long pendingGradingCount) { this.pendingGradingCount = pendingGradingCount; }
    public Long getGradedCount() { return gradedCount; }
    public void setGradedCount(Long gradedCount) { this.gradedCount = gradedCount; }
    public Long getDuplicateCount() { return duplicateCount; }
    public void setDuplicateCount(Long duplicateCount) { this.duplicateCount = duplicateCount; }
    public Long getUnreadNotificationCount() { return unreadNotificationCount; }
    public void setUnreadNotificationCount(Long unreadNotificationCount) { this.unreadNotificationCount = unreadNotificationCount; }
    public List<CourseApplicationSummary> getRecentApplications() { return recentApplications; }
    public void setRecentApplications(List<CourseApplicationSummary> recentApplications) { this.recentApplications = recentApplications; }
    public List<CourseSummary> getRecentCourses() { return recentCourses; }
    public void setRecentCourses(List<CourseSummary> recentCourses) { this.recentCourses = recentCourses; }
    public List<OperationLog> getRecentLogs() { return recentLogs; }
    public void setRecentLogs(List<OperationLog> recentLogs) { this.recentLogs = recentLogs; }
    public List<Notification> getRecentNotifications() { return recentNotifications; }
    public void setRecentNotifications(List<Notification> recentNotifications) { this.recentNotifications = recentNotifications; }
}
