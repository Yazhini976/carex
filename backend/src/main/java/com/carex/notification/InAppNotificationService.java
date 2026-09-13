package com.carex.notification;

import com.carex.entity.Notification;
import com.carex.notification.dto.NotificationStatsResponse;

import java.util.List;

/**
 * Service managing persistent in-app notifications stored in the PostgreSQL database.
 */
public interface InAppNotificationService {

    Notification recordNotification(NotificationEvent event);

    List<Notification> getNotificationsForUser(Long userId);

    long getUnreadCount(Long userId);

    Notification markRead(Long id);

    void markAllRead(Long userId);

    NotificationStatsResponse getStats();
}
