package com.coursework.system.notification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.notification.entity.Notification;

import java.util.List;

public interface NotificationService extends IService<Notification> {
    Notification create(Long userId, String type, String title, String content);

    Notification create(Long userId, String type, String title, String content,
                        String targetType, Long targetId, Long courseId);

    List<Notification> listForUser(Long userId);

    List<Notification> listForUser(Long userId, Long courseId);

    List<Notification> listForCourse(Long courseId, UserPrincipal principal);

    void markRead(Long notificationId, Long userId);

    void markAllRead(Long userId);

    long unreadCount(Long userId);
}
