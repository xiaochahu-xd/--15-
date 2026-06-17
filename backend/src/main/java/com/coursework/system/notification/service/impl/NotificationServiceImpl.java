package com.coursework.system.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursework.system.common.exception.BusinessException;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.course.service.CourseService;
import com.coursework.system.notification.entity.Notification;
import com.coursework.system.notification.mapper.NotificationMapper;
import com.coursework.system.notification.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    private final CourseService courseService;

    public NotificationServiceImpl(CourseService courseService) {
        this.courseService = courseService;
    }

    @Override
    public Notification create(Long userId, String type, String title, String content) {
        return create(userId, type, title, content, null, null, null);
    }

    @Override
    public Notification create(Long userId, String type, String title, String content,
                               String targetType, Long targetId, Long courseId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setCourseId(courseId);
        notification.setRead(0);
        notification.setCreatedAt(LocalDateTime.now());
        save(notification);
        return notification;
    }

    @Override
    public List<Notification> listForUser(Long userId) {
        return listForUser(userId, null);
    }

    @Override
    public List<Notification> listForUser(Long userId, Long courseId) {
        QueryWrapper<Notification> wrapper = new QueryWrapper<Notification>()
                .eq("receiver_id", userId);
        if (courseId != null) {
            wrapper.eq("course_id", courseId);
        }
        return list(wrapper
                .orderByDesc("created_at")
                .last("LIMIT 100"));
    }

    @Override
    public List<Notification> listForCourse(Long courseId, UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(401, "Please login first");
        }
        if (!courseService.canViewCourse(courseId, principal)) {
            throw new BusinessException(403, "No permission to view course notifications");
        }
        QueryWrapper<Notification> wrapper = new QueryWrapper<Notification>()
                .eq("course_id", courseId);
        if (!principal.getRoles().contains("ADMIN")) {
            wrapper.eq("receiver_id", principal.getId());
        }
        return list(wrapper
                .orderByDesc("created_at")
                .last("LIMIT 100"));
    }

    @Override
    public void markRead(Long notificationId, Long userId) {
        Notification notification = getById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            throw new BusinessException(404, "通知不存在");
        }
        notification.setRead(1);
        updateById(notification);
    }

    @Override
    public void markAllRead(Long userId) {
        List<Notification> notifications = list(new QueryWrapper<Notification>()
                .eq("receiver_id", userId)
                .eq("read_status", 0));
        for (Notification notification : notifications) {
            notification.setRead(1);
            updateById(notification);
        }
    }

    @Override
    public long unreadCount(Long userId) {
        return count(new QueryWrapper<Notification>()
                .eq("receiver_id", userId)
                .eq("read_status", 0));
    }
}
