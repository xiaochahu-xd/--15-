package com.coursework.system.event;

import java.time.LocalDateTime;

public interface SystemEvent {
    String getEventType();

    Long getActorId();

    String getActorUsername();

    String getTargetType();

    Long getTargetId();

    String getIp();

    LocalDateTime getOccurredAt();
}
