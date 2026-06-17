package com.coursework.system.event;

public interface EventHandler {
    boolean supports(SystemEvent event);

    void handle(SystemEvent event);
}
