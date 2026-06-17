package com.coursework.system.event;

import java.time.LocalDateTime;

public class SubmissionCreatedEvent implements SystemEvent {
    private final Long submissionId;
    private final Long assignmentId;
    private final String assignmentTitle;
    private final Long studentId;
    private final String studentUsername;
    private final String ip;
    private final LocalDateTime occurredAt;

    public SubmissionCreatedEvent(Long submissionId, Long assignmentId, String assignmentTitle,
                                  Long studentId, String studentUsername, String ip) {
        this.submissionId = submissionId;
        this.assignmentId = assignmentId;
        this.assignmentTitle = assignmentTitle;
        this.studentId = studentId;
        this.studentUsername = studentUsername;
        this.ip = ip;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public Long getStudentId() {
        return studentId;
    }

    @Override
    public String getEventType() {
        return "SUBMISSION_CREATED";
    }

    @Override
    public Long getActorId() {
        return studentId;
    }

    @Override
    public String getActorUsername() {
        return studentUsername;
    }

    @Override
    public String getTargetType() {
        return "SUBMISSION";
    }

    @Override
    public Long getTargetId() {
        return submissionId;
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
