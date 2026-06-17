package com.coursework.system.event;

import java.time.LocalDateTime;

public class CourseApplicationCreatedEvent implements SystemEvent {
    private final Long applicationId;
    private final Long teacherId;
    private final String teacherUsername;
    private final String courseName;
    private final String courseCode;
    private final String ip;
    private final LocalDateTime occurredAt;

    public CourseApplicationCreatedEvent(Long applicationId, Long teacherId, String teacherUsername,
                                         String courseName, String courseCode, String ip) {
        this.applicationId = applicationId;
        this.teacherId = teacherId;
        this.teacherUsername = teacherUsername;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.ip = ip;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public String getTeacherUsername() {
        return teacherUsername;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    @Override
    public String getEventType() {
        return "COURSE_APPLICATION_CREATED";
    }

    @Override
    public Long getActorId() {
        return teacherId;
    }

    @Override
    public String getActorUsername() {
        return teacherUsername;
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
