package com.carex.service;

import com.carex.entity.Notification;
import com.carex.notification.NotificationEvent;
import com.carex.notification.dto.NotificationStatsResponse;

import java.util.List;

public interface NotificationService {
    Notification createNotification(Long userId, Long appointmentId, String type, String message);
    Notification createSystemNotification(Long userId, String type, String message);
    List<Notification> getNotificationsForUser(Long userId);
    List<Notification> getPendingNotifications();
    Notification markSent(Long id);
    Notification markFailed(Long id);
    Notification markRead(Long id);
    void markAllRead(Long userId);
    long getUnreadCount(Long userId);
    NotificationStatsResponse getStats();
    void sendNotificationEvent(NotificationEvent event);
}

