package com.coursework.system.event;

public interface EventManager {
    void publish(SystemEvent event);

    void publishUserNotification(Long userId, String type, String title, String content);

    void publishAssignmentPublished(AssignmentPublishedEvent event);

    void publishGradeCompleted(GradeCompletedEvent event);

    void publishAssignmentReturned(AssignmentReturnedEvent event);
}
