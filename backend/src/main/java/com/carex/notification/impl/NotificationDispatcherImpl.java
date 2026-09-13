package com.carex.notification.impl;

import com.carex.notification.EmailNotificationService;
import com.carex.notification.InAppNotificationService;
import com.carex.notification.NotificationChannel;
import com.carex.notification.NotificationDispatcher;
import com.carex.notification.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationDispatcherImpl implements NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcherImpl.class);

    private final InAppNotificationService inAppNotificationService;
    private final EmailNotificationService emailNotificationService;

    public NotificationDispatcherImpl(InAppNotificationService inAppNotificationService,
                                      EmailNotificationService emailNotificationService) {
        this.inAppNotificationService = inAppNotificationService;
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    public void dispatch(NotificationEvent event) {
        if (event == null) {
            log.warn("Cannot dispatch null NotificationEvent");
            return;
        }

        NotificationChannel channel = event.getChannel() != null
                ? event.getChannel()
                : NotificationChannel.ALL;

        log.info("[NOTIFICATION-DISPATCH] Dispatching event type={} priority={} channel={} recipientUserId={}",
                event.getNotificationType(), event.getPriority(), channel, event.getRecipientUserId());

        // Channel 1: In-App Notification
        if (channel == NotificationChannel.IN_APP || channel == NotificationChannel.ALL) {
            try {
                inAppNotificationService.recordNotification(event);
            } catch (Exception ex) {
                log.error("[NOTIFICATION-INAPP-ERROR] Failed to save in-app notification: {}", ex.getMessage(), ex);
            }
        }

        // Channel 2: Email Notification (Async / non-blocking)
        if (channel == NotificationChannel.EMAIL || channel == NotificationChannel.ALL) {
            try {
                emailNotificationService.send(event);
            } catch (Exception ex) {
                log.error("[NOTIFICATION-EMAIL-ERROR] Failed to dispatch email notification: {}", ex.getMessage(), ex);
            }
        }
    }
}
