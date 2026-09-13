package com.carex.notification;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates a notification event payload dispatched across CAREX notification channels.
 */
public class NotificationEvent {

    private Long recipientUserId;
    private String recipientEmail;
    private String recipientName;
    private Long appointmentId;
    private NotificationType notificationType;
    private NotificationPriority priority;
    private NotificationChannel channel;
    private String subject;
    private String message;
    private String htmlBody;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;

    public NotificationEvent() {
        this.priority = NotificationPriority.NORMAL;
        this.channel = NotificationChannel.ALL;
        this.metadata = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(Long recipientUserId) { this.recipientUserId = recipientUserId; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }

    public NotificationPriority getPriority() { return priority; }
    public void setPriority(NotificationPriority priority) { this.priority = priority; }

    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getHtmlBody() { return htmlBody; }
    public void setHtmlBody(String htmlBody) { this.htmlBody = htmlBody; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public static class Builder {
        private final NotificationEvent event = new NotificationEvent();

        public Builder recipientUserId(Long recipientUserId) {
            event.recipientUserId = recipientUserId;
            return this;
        }

        public Builder recipientEmail(String recipientEmail) {
            event.recipientEmail = recipientEmail;
            return this;
        }

        public Builder recipientName(String recipientName) {
            event.recipientName = recipientName;
            return this;
        }

        public Builder appointmentId(Long appointmentId) {
            event.appointmentId = appointmentId;
            return this;
        }

        public Builder notificationType(NotificationType notificationType) {
            event.notificationType = notificationType;
            return this;
        }

        public Builder priority(NotificationPriority priority) {
            event.priority = priority;
            return this;
        }

        public Builder channel(NotificationChannel channel) {
            event.channel = channel;
            return this;
        }

        public Builder subject(String subject) {
            event.subject = subject;
            return this;
        }

        public Builder message(String message) {
            event.message = message;
            return this;
        }

        public Builder htmlBody(String htmlBody) {
            event.htmlBody = htmlBody;
            return this;
        }

        public Builder metadata(String key, Object value) {
            if (event.metadata == null) {
                event.metadata = new HashMap<>();
            }
            event.metadata.put(key, value);
            return this;
        }

        public NotificationEvent build() {
            return event;
        }
    }

    @Override
    public String toString() {
        return "NotificationEvent{" +
                "recipientUserId=" + recipientUserId +
                ", appointmentId=" + appointmentId +
                ", type=" + notificationType +
                ", priority=" + priority +
                ", channel=" + channel +
                '}';
    }
}
