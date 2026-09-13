package com.carex.notification;

/**
 * Service for dispatching email notifications via SMTP.
 */
public interface EmailNotificationService {

    /**
     * Sends an email for the given notification event.
     * Must handle exceptions gracefully without throwing errors back to callers.
     *
     * @param event the notification payload to send
     */
    void send(NotificationEvent event);

    /**
     * Checks if email delivery is currently enabled.
     *
     * @return true if enabled, false if running in development / disabled mode
     */
    boolean isEmailEnabled();
}
