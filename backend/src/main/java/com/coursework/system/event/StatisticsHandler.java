package com.coursework.system.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StatisticsHandler implements EventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsHandler.class);

    @Override
    public boolean supports(SystemEvent event) {
        return event instanceof SubmissionCreatedEvent
                || event instanceof GradeCompletedEvent
                || event instanceof AssignmentReturnedEvent;
    }

    @Override
    public void handle(SystemEvent event) {
        LOGGER.debug("统计事件预留处理：{}", event.getEventType());
    }
}
