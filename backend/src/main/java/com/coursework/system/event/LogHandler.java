package com.coursework.system.event;

import com.coursework.system.log.service.OperationLogService;
import org.springframework.stereotype.Component;

@Component
public class LogHandler implements EventHandler {
    private final OperationLogService operationLogService;

    public LogHandler(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Override
    public boolean supports(SystemEvent event) {
        return event instanceof GradeCompletedEvent || event instanceof AssignmentReturnedEvent
                || event instanceof SubmissionCreatedEvent || event instanceof CourseApplicationCreatedEvent
                || event instanceof CourseApprovedEvent || event instanceof AssignmentPublishedEvent;
    }

    @Override
    public void handle(SystemEvent event) {
        if (event instanceof GradeCompletedEvent) {
            handleGradeCompleted((GradeCompletedEvent) event);
        } else if (event instanceof AssignmentReturnedEvent) {
            handleAssignmentReturned((AssignmentReturnedEvent) event);
        } else if (event.getActorId() != null) {
            operationLogService.record(event.getActorId(), event.getActorUsername(),
                    event.getEventType(), event.getTargetType(), event.getTargetId(),
                    event.getIp(), "SUCCESS", "事件已发布：" + event.getEventType());
        }
    }

    private void handleGradeCompleted(GradeCompletedEvent event) {
        operationLogService.record(event.getGraderId(), event.getGraderUsername(), "GRADE_COMPLETE",
                "SUBMISSION", event.getSubmissionId(), event.getIp(), "SUCCESS",
                "完成作业批改，作业ID：" + event.getAssignmentId() + "，分数：" + event.getScore());
    }

    private void handleAssignmentReturned(AssignmentReturnedEvent event) {
        operationLogService.record(event.getGraderId(), event.getGraderUsername(), "ASSIGNMENT_RETURN",
                "SUBMISSION", event.getSubmissionId(), event.getIp(), "SUCCESS",
                "退回作业修改，作业ID：" + event.getAssignmentId() + "，原因：" + event.getReason());
    }
}
