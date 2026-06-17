package com.coursework.system.event;

import com.coursework.system.course.dto.CourseMemberSummary;
import com.coursework.system.course.service.CourseMemberService;
import com.coursework.system.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SimpleEventManager implements EventManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleEventManager.class);

    private final NotificationService notificationService;
    private final CourseMemberService courseMemberService;
    private final List<EventHandler> handlers;

    public SimpleEventManager(NotificationService notificationService,
                              CourseMemberService courseMemberService,
                              List<EventHandler> handlers) {
        this.notificationService = notificationService;
        this.courseMemberService = courseMemberService;
        this.handlers = handlers;
    }

    @Override
    public void publish(SystemEvent event) {
        for (EventHandler handler : handlers) {
            try {
                if (handler.supports(event)) {
                    handler.handle(event);
                }
            } catch (Exception exception) {
                LOGGER.warn("事件处理失败，eventType={}, handler={}",
                        event.getEventType(), handler.getClass().getSimpleName(), exception);
            }
        }
    }

    @Override
    public void publishUserNotification(Long userId, String type, String title, String content) {
        notificationService.create(userId, type, title, content);
    }

    @Override
    public void publishAssignmentPublished(AssignmentPublishedEvent event) {
        publish(event);
    }

    @Override
    public void publishGradeCompleted(GradeCompletedEvent event) {
        publish(event);
    }

    @Override
    public void publishAssignmentReturned(AssignmentReturnedEvent event) {
        publish(event);
    }
}
