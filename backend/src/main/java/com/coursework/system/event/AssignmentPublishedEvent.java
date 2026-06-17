package com.coursework.system.event;

import java.time.LocalDateTime;

public class AssignmentPublishedEvent implements SystemEvent {
    private final Long courseId;
    private final Long assignmentId;
    private final String assignmentTitle;
    private final Long publisherId;
    private final String publisherUsername;
    private final String ip;
    private final LocalDateTime occurredAt;

    public AssignmentPublishedEvent(Long courseId, Long assignmentId, String assignmentTitle, Long publisherId) {
        this(courseId, assignmentId, assignmentTitle, publisherId, null, null);
    }

    public AssignmentPublishedEvent(Long courseId, Long assignmentId, String assignmentTitle,
                                    Long publisherId, String publisherUsername, String ip) {
        this.courseId = courseId;
        this.assignmentId = assignmentId;
        this.assignmentTitle = assignmentTitle;
        this.publisherId = publisherId;
        this.publisherUsername = publisherUsername;
        this.ip = ip;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    @Override
    public String getEventType() {
        return "ASSIGNMENT_PUBLISHED";
    }

    @Override
    public Long getActorId() {
        return publisherId;
    }

    @Override
    public String getActorUsername() {
        return publisherUsername;
    }

    @Override
    public String getTargetType() {
        return "ASSIGNMENT";
    }

    @Override
    public Long getTargetId() {
        return assignmentId;
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
