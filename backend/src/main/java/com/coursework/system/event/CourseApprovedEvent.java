package com.coursework.system.event;

import java.time.LocalDateTime;

public class CourseApprovedEvent implements SystemEvent {
    private final Long applicationId;
    private final Long teacherId;
    private final String courseName;
    private final String courseCode;
    private final boolean approved;
    private final String rejectReason;
    private final Long reviewerId;
    private final String reviewerUsername;
    private final String ip;
    private final LocalDateTime occurredAt;

    public CourseApprovedEvent(Long applicationId, Long teacherId, String courseName, String courseCode,
                               boolean approved, String rejectReason, Long reviewerId,
                               String reviewerUsername, String ip) {
        this.applicationId = applicationId;
        this.teacherId = teacherId;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.approved = approved;
        this.rejectReason = rejectReason;
        this.reviewerId = reviewerId;
        this.reviewerUsername = reviewerUsername;
        this.ip = ip;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public boolean isApproved() {
        return approved;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    @Override
    public String getEventType() {
        return approved ? "COURSE_APPROVED" : "COURSE_REJECTED";
    }

    @Override
    public Long getActorId() {
        return reviewerId;
    }

    @Override
    public String getActorUsername() {
        return reviewerUsername;
    }

    @Override
    public String getTargetType() {
        return "COURSE_APPLICATION";
    }

    @Override
    public Long getTargetId() {
        return applicationId;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
