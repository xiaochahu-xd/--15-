package com.coursework.system.event;

import java.time.LocalDateTime;

public class AssignmentReturnedEvent implements SystemEvent {
    private final Long submissionId;
    private final Long assignmentId;
    private final String assignmentTitle;
    private final Long studentId;
    private final Long graderId;
    private final String graderUsername;
    private final String reason;
    private final String ip;
    private final LocalDateTime occurredAt;

    public AssignmentReturnedEvent(Long submissionId, Long assignmentId, String assignmentTitle, Long studentId,
                                   Long graderId, String graderUsername, String reason, String ip) {
        this.submissionId = submissionId;
        this.assignmentId = assignmentId;
        this.assignmentTitle = assignmentTitle;
        this.studentId = studentId;
        this.graderId = graderId;
        this.graderUsername = graderUsername;
        this.reason = reason;
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

    public Long getGraderId() {
        return graderId;
    }

    public String getGraderUsername() {
        return graderUsername;
    }

    public String getReason() {
        return reason;
    }

    public String getIp() {
        return ip;
    }

    @Override
    public String getEventType() {
        return "ASSIGNMENT_RETURNED";
    }

    @Override
    public Long getActorId() {
        return graderId;
    }

    @Override
    public String getActorUsername() {
        return graderUsername;
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
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
